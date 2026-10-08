package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.DTO.ExtractionResult;
import com.nawaf.capstone3.DTO.MaintenanceRuleAiDto;
import com.nawaf.capstone3.Enum.ManualStatus;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.UserManual;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.UserManualRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

// ⚠ تعديل: هذا هو ManualAnalysisService القديم بعد إعادة تسميته (الحل لمشكلة @Async)
@Service
public class ManualAnalysisWorker {

    private static final Logger log = LoggerFactory.getLogger(ManualAnalysisWorker.class);

    private static final int DIRECT_LIMIT_PAGES = 30;   // أقل من هذا: نحلل الملف كله بدون مرور أول
    private static final int PAGES_PER_BATCH = 15;
    private static final int MAX_PDF_BYTES = 50 * 1024 * 1024;

    private final UserManualRepository manualRepo;
    private final MaintenanceRuleRepository ruleRepo;
    private final PdfPageService pdfService;
    private final MaintenancePageFinder pageFinder;
    private final GeminiCaller gemini;
    private final TransactionTemplate tx;
    private final RestClient restClient = RestClient.create();

    public ManualAnalysisWorker(UserManualRepository manualRepo,
                                MaintenanceRuleRepository ruleRepo,
                                PdfPageService pdfService,
                                MaintenancePageFinder pageFinder,
                                GeminiCaller gemini,
                                TransactionTemplate tx) {
        this.manualRepo = manualRepo;
        this.ruleRepo = ruleRepo;
        this.pdfService = pdfService;
        this.pageFinder = pageFinder;
        this.gemini = gemini;
        this.tx = tx;
    }

    @Async
    public void analyze(Integer manualId) {                       // ⚠ Integer بدل Long
        // ⚠ الحالة ANALYZING وُضعت مسبقاً في ManualAnalysisService.startAnalysis (claimForAnalysis)
        String fileUrl;
        try {
            fileUrl = manualRepo.findById(manualId)
                    .orElseThrow(() -> new IllegalArgumentException("Manual not found: " + manualId))
                    .getFileUrl();
        } catch (Exception e) {
            log.error("Cannot load manual {}", manualId, e);
            return;
        }

        try {
            byte[] pdf = download(fileUrl);
            int total = pdfService.pageCount(pdf);

            // المرور الأول (أو تخطيه للملفات الصغيرة)
            List<Integer> pages = total <= DIRECT_LIMIT_PAGES
                    ? IntStream.rangeClosed(1, total).boxed().toList()
                    : pageFinder.find(pdf, total);

            if (pages.isEmpty()) {
                throw new IllegalStateException("No maintenance pages found in manual " + manualId);
            }
            log.info("Manual {}: analysing {} of {} pages", manualId, pages.size(), total);

            // المرور الثاني على دفعات
            List<MaintenanceRuleAiDto> raw = new ArrayList<>();
            for (List<Integer> batch : partition(pages, PAGES_PER_BATCH)) {
                byte[] part = pdfService.extractPages(pdf, batch);
                ExtractionResult r = gemini.extract(
                        Prompts.EXTRACT_RULES_SYSTEM,
                        "Extract all maintenance rules from the attached manual pages.",
                        part,
                        ExtractionResult.class);
                if (r != null && r.rules() != null) raw.addAll(r.rules());
            }

            List<MaintenanceRuleAiDto> valid = deduplicate(raw).stream()
                    .filter(this::isValid)
                    .toList();

            // نحذف القديم ونحفظ الجديد في معاملة واحدة، وفقط بعد نجاح الاستخراج
            tx.executeWithoutResult(s -> {
                UserManual manualRef = manualRepo.getReferenceById(manualId);   // ⚠ مرجع داخل المعاملة
                ruleRepo.deleteByUserManualId(manualId);                         // ⚠ كان deleteByManualId
                ruleRepo.saveAll(valid.stream().map(d -> toEntity(d, manualRef)).toList());
            });

            manualRepo.updateStatus(manualId, ManualStatus.COMPLETED.name());    // ⚠ بدل save(manual)
            log.info("Manual {}: saved {} rules", manualId, valid.size());

        } catch (Exception e) {
            log.error("Manual analysis failed for id={}", manualId, e);
            manualRepo.updateStatus(manualId, ManualStatus.FAILED.name());       // ⚠ بدل save(manual)
        }
    }

    // ---------- تنزيل الملف مع حماية بسيطة ----------

    private byte[] download(String url) {
        URI uri = URI.create(url);
        assertSafe(uri);

        byte[] data = restClient.get().uri(uri).retrieve().body(byte[].class);
        if (data == null || data.length == 0 || data.length > MAX_PDF_BYTES) {
            throw new IllegalStateException("PDF is empty or too large");
        }
        boolean isPdf = data.length > 4 && data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F';
        if (!isPdf) throw new IllegalStateException("File is not a PDF");
        return data;
    }

    private void assertSafe(URI uri) {
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalArgumentException("Only https URLs are allowed");
        }
        try {
            for (InetAddress a : InetAddress.getAllByName(uri.getHost())) {
                if (a.isLoopbackAddress() || a.isSiteLocalAddress() || a.isLinkLocalAddress()
                        || a.isAnyLocalAddress() || a.isMulticastAddress()) {
                    throw new IllegalArgumentException("URL points to a private address");
                }
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host: " + uri.getHost());
        }
    }

    // ---------- دمج وفلترة وتحويل ----------

    private List<MaintenanceRuleAiDto> deduplicate(List<MaintenanceRuleAiDto> rules) {
        Map<String, MaintenanceRuleAiDto> map = new LinkedHashMap<>();
        for (MaintenanceRuleAiDto r : rules) {
            if (r.serviceName() == null || r.triggerType() == null) continue;
            String key = r.serviceName().trim().toLowerCase() + "|" + r.triggerType();
            map.merge(key, r, (old, cur) -> completeness(cur) > completeness(old) ? cur : old);
        }
        return new ArrayList<>(map.values());
    }

    private int completeness(MaintenanceRuleAiDto d) {
        int s = 0;
        if (d.kilometerInterval() != null) s++;
        if (d.monthInterval() != null) s++;
        if (d.description() != null) s++;
        if (d.condition() != null) s++;
        if (d.notes() != null) s++;
        return s;
    }

    private boolean isValid(MaintenanceRuleAiDto d) {
        if (d.serviceName() == null || d.serviceName().isBlank() || d.triggerType() == null) return false;
        if (d.kilometerInterval() != null && d.kilometerInterval() <= 0) return false;
        if (d.monthInterval() != null && d.monthInterval() <= 0) return false;

        return switch (d.triggerType()) {
            case KILOMETER         -> d.kilometerInterval() != null;
            case TIME              -> d.monthInterval() != null;
            case KILOMETER_OR_TIME -> d.kilometerInterval() != null && d.monthInterval() != null;
            case CONDITION         -> d.condition() != null && !d.condition().isBlank();
        };
    }

    private MaintenanceRule toEntity(MaintenanceRuleAiDto d, UserManual manual) {
        MaintenanceRule r = new MaintenanceRule();
        r.setUserManual(manual);
        r.setServiceName(cut(d.serviceName(), 100));      // ⚠ تعديل
        r.setDescription(cut(d.description(), 500));      // ⚠ تعديل
        r.setTriggerType(d.triggerType().name());
        r.setKilometerInterval(d.kilometerInterval());
        r.setMonthInterval(d.monthInterval());
        r.setCondition(cut(d.condition(), 500));          // ⚠ تعديل
        r.setNotes(cut(d.notes(), 1000));                 // ⚠ تعديل
        return r;
    }
    // ⚠ جديد: يقص النص للحد المسموح ويحول الفارغ إلى null
    private static String cut(String s, int max) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        return t.length() <= max ? t : t.substring(0, max);
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            out.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return out;
    }
}