package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Client.OpenRouterClient;
import com.nawaf.capstone3.DTO.ExtractionResult;
import com.nawaf.capstone3.DTO.MaintenanceRuleAiDto;
import com.nawaf.capstone3.Enum.ManualStatus;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.UserManual;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.UserManualRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class ManualAnalysisWorker {

    private static final Logger log = LoggerFactory.getLogger(ManualAnalysisWorker.class);

    private static final int DIRECT_LIMIT_PAGES = 30;
    private static final int PAGES_PER_BATCH = 15;
    private static final int MAX_PDF_BYTES = 150 * 1024 * 1024;

    private final UserManualRepository manualRepo;
    private final MaintenanceRuleRepository ruleRepo;
    private final PdfPageService pdfService;
    private final MaintenancePageFinder pageFinder;
    private final MaintenancePageScanner pageScanner;
    private final OpenRouterClient openRouterClient;
    private final TransactionTemplate tx;

    private final RestClient restClient = createDownloadClient();

    public ManualAnalysisWorker(UserManualRepository manualRepo, MaintenanceRuleRepository ruleRepo, PdfPageService pdfService, MaintenancePageFinder pageFinder, MaintenancePageScanner pageScanner, OpenRouterClient openRouterClient, TransactionTemplate tx) {
        this.manualRepo = manualRepo;
        this.ruleRepo = ruleRepo;
        this.pdfService = pdfService;
        this.pageFinder = pageFinder;
        this.pageScanner = pageScanner;
        this.openRouterClient = openRouterClient;
        this.tx = tx;
    }

    private static RestClient createDownloadClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMinutes(3));

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Async
    public void analyze(Integer manualId) {
        String fileUrl;

        try {
            fileUrl = manualRepo.findById(manualId)
                    .orElseThrow(() -> new IllegalArgumentException("Manual not found: " + manualId))
                    .getFileUrl();
        } catch (Exception exception) {
            log.error("Cannot load manual {}", manualId, exception);
            manualRepo.updateStatus(manualId, ManualStatus.FAILED.name());
            return;
        }

        try {
            byte[] pdf = download(fileUrl);
            int totalPages = pdfService.pageCount(pdf);

            log.info("Manual {} downloaded successfully. Total pages: {}", manualId, totalPages);

            List<Integer> pages = findMaintenancePages(manualId, pdf, totalPages);

            if (pages.isEmpty())
                throw new IllegalStateException("No maintenance pages found in manual " + manualId);

            log.info(
                    "Manual {}: analysing {} of {} pages",
                    manualId,
                    pages.size(),
                    totalPages
            );

            List<MaintenanceRuleAiDto> rawRules = extractRules(manualId, pdf, pages);

            List<MaintenanceRuleAiDto> validRules = deduplicate(rawRules);

            log.info(
                    "Manual {}: received {} rules; {} remain after validation and deduplication",
                    manualId,
                    rawRules.size(),
                    validRules.size()
            );

            if (validRules.isEmpty())
                throw new IllegalStateException("No valid maintenance rules extracted for manual " + manualId);

            saveRules(manualId, validRules);

            manualRepo.updateStatus(manualId, ManualStatus.COMPLETED.name());

            log.info(
                    "Manual {} analysis completed successfully. Saved {} rules",
                    manualId,
                    validRules.size()
            );

        } catch (Exception exception) {
            log.error("Manual analysis failed for id={}", manualId, exception);
            manualRepo.updateStatus(manualId, ManualStatus.FAILED.name());
        }
    }

    private List<Integer> findMaintenancePages(Integer manualId, byte[] pdf, int totalPages) throws Exception {
        if (totalPages <= DIRECT_LIMIT_PAGES) {
            log.info(
                    "Manual {} is small. Analysing all {} pages directly",
                    manualId,
                    totalPages
            );

            return IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .toList();
        }

        log.info(
                "Manual {} is large. Running local maintenance page scan before AI",
                manualId
        );

        List<Integer> pages = pageScanner.findCandidatePages(pdf);

        if (!pages.isEmpty()) {
            log.info(
                    "Local scan selected {} candidate pages from {} total pages",
                    pages.size(),
                    totalPages
            );

            return pages;
        }

        log.info(
                "Local scan found no maintenance pages for manual {}. Falling back to AI page detection",
                manualId
        );

        pages = pageFinder.find(pdf, totalPages);

        log.info(
                "AI page detection selected {} pages from {} total pages",
                pages.size(),
                totalPages
        );

        return pages;
    }

    private List<MaintenanceRuleAiDto> extractRules(Integer manualId, byte[] pdf, List<Integer> pages) throws Exception {
        List<MaintenanceRuleAiDto> rules = new ArrayList<>();
        List<List<Integer>> batches = partition(pages, PAGES_PER_BATCH);

        log.info(
                "Manual {}: extracting rules using {} batches",
                manualId,
                batches.size()
        );

        for (int index = 0; index < batches.size(); index++) {
            List<Integer> batch = batches.get(index);
            int batchNumber = index + 1;

            log.info(
                    "Manual {}: extracting batch {}/{}, pages={}",
                    manualId,
                    batchNumber,
                    batches.size(),
                    batch
            );

            long started = System.currentTimeMillis();

            byte[] part = pdfService.extractPages(pdf, batch);

            ExtractionResult result = openRouterClient.analyzePdf(
                    Prompts.EXTRACT_RULES_SYSTEM,
                    "Extract all maintenance rules from the attached manual pages.",
                    part,
                    ExtractionResult.class
            );

            int extractedCount = 0;

            if (result != null && result.rules() != null) {
                rules.addAll(result.rules());
                extractedCount = result.rules().size();
            }

            log.info(
                    "Manual {}: batch {}/{} returned {} rules in {} seconds",
                    manualId,
                    batchNumber,
                    batches.size(),
                    extractedCount,
                    (System.currentTimeMillis() - started) / 1000
            );
        }

        return rules;
    }

    private void saveRules(Integer manualId, List<MaintenanceRuleAiDto> rules) {
        tx.executeWithoutResult(status -> {
            UserManual manual = manualRepo.getReferenceById(manualId);

            ruleRepo.deleteByUserManualId(manualId);

            List<MaintenanceRule> entities = rules.stream()
                    .map(rule -> toEntity(rule, manual))
                    .toList();

            ruleRepo.saveAll(entities);
        });
    }

    private byte[] download(String url) {
        URI uri = URI.create(url);

        assertSafe(uri);

        byte[] data = restClient.get()
                .uri(uri)
                .header("Accept", "application/pdf")
                .header("Accept-Encoding", "identity")
                .exchange((request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful())
                        throw new IllegalStateException(
                                "PDF download failed: HTTP " + response.getStatusCode().value()
                        );

                    long contentLength = response.getHeaders().getContentLength();

                    if (contentLength > MAX_PDF_BYTES)
                        throw new IllegalStateException("PDF is too large");

                    try (var input = response.getBody()) {
                        return input.readNBytes(MAX_PDF_BYTES + 1);
                    }
                });

        if (data == null || data.length == 0)
            throw new IllegalStateException("PDF is empty");

        if (data.length > MAX_PDF_BYTES)
            throw new IllegalStateException("PDF is too large");

        if (!isPdf(data))
            throw new IllegalStateException("File is not a PDF");

        log.info("PDF downloaded successfully: {} bytes", data.length);

        return data;
    }

    private boolean isPdf(byte[] data) {
        return data.length >= 5
                && data[0] == '%'
                && data[1] == 'P'
                && data[2] == 'D'
                && data[3] == 'F'
                && data[4] == '-';
    }

    private void assertSafe(URI uri) {
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null)
            throw new IllegalArgumentException("Only https URLs are allowed");

        try {
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (address.isLoopbackAddress()
                        || address.isSiteLocalAddress()
                        || address.isLinkLocalAddress()
                        || address.isAnyLocalAddress()
                        || address.isMulticastAddress())
                    throw new IllegalArgumentException("URL points to a private address");
            }

        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException(
                    "Unknown host: " + uri.getHost(),
                    exception
            );
        }
    }

    private List<MaintenanceRuleAiDto> deduplicate(List<MaintenanceRuleAiDto> rules) {
        Map<String, MaintenanceRuleAiDto> unique = new LinkedHashMap<>();

        for (MaintenanceRuleAiDto rule : rules) {
            if (rule == null) continue;
            if (!isValid(rule)) continue;

            String key = normalize(rule.serviceName())
                    + "|" + rule.triggerType().name()
                    + "|" + rule.kilometerInterval()
                    + "|" + rule.monthInterval()
                    + "|" + normalize(rule.condition());

            unique.merge(
                    key,
                    rule,
                    (oldRule, newRule) ->
                            completeness(newRule) > completeness(oldRule)
                                    ? newRule
                                    : oldRule
            );
        }

        return new ArrayList<>(unique.values());
    }

    private static String normalize(String value) {
        if (value == null) return "";

        return value.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private int completeness(MaintenanceRuleAiDto rule) {
        int score = 0;

        if (rule.kilometerInterval() != null) score++;
        if (rule.monthInterval() != null) score++;
        if (rule.description() != null) score++;
        if (rule.condition() != null) score++;
        if (rule.notes() != null) score++;

        return score;
    }

    private boolean isValid(MaintenanceRuleAiDto rule) {
        if (rule.serviceName() == null || rule.serviceName().isBlank() || rule.triggerType() == null)
            return false;

        if (rule.kilometerInterval() != null && rule.kilometerInterval() <= 0)
            return false;

        if (rule.monthInterval() != null && rule.monthInterval() <= 0)
            return false;

        return switch (rule.triggerType()) {
            case KILOMETER -> rule.kilometerInterval() != null;

            case TIME -> rule.monthInterval() != null;

            case KILOMETER_OR_TIME ->
                    rule.kilometerInterval() != null
                            && rule.monthInterval() != null;

            case CONDITION ->
                    rule.condition() != null
                            && !rule.condition().isBlank();
        };
    }

    private MaintenanceRule toEntity(MaintenanceRuleAiDto dto, UserManual manual) {
        MaintenanceRule rule = new MaintenanceRule();

        rule.setUserManual(manual);
        rule.setServiceName(cut(dto.serviceName(), 100));
        rule.setDescription(cut(dto.description(), 500));
        rule.setTriggerType(dto.triggerType().name());
        rule.setKilometerInterval(dto.kilometerInterval());
        rule.setMonthInterval(dto.monthInterval());
        rule.setCondition(cut(dto.condition(), 500));
        rule.setNotes(cut(dto.notes(), 1000));

        return rule;
    }

    private static String cut(String value, int maxLength) {
        if (value == null) return null;

        String trimmed = value.trim();

        if (trimmed.isEmpty()) return null;

        return trimmed.length() <= maxLength
                ? trimmed
                : trimmed.substring(0, maxLength);
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();

        for (int index = 0; index < list.size(); index += size) {
            partitions.add(
                    list.subList(
                            index,
                            Math.min(index + size, list.size())
                    )
            );
        }

        return partitions;
    }
}