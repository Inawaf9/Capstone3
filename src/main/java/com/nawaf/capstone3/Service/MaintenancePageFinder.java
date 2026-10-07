package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Client.OpenRouterClient;
import com.nawaf.capstone3.DTO.PageDetection;
import com.nawaf.capstone3.DTO.PdfChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class MaintenancePageFinder {

    private static final Logger log = LoggerFactory.getLogger(MaintenancePageFinder.class);

    private static final int CHUNK_PAGES = 25;
    private static final int MARGIN = 1;
    private static final int MAX_PAGES = 80;

    private final PdfPageService pdfService;
    private final OpenRouterClient openRouterClient;

    public MaintenancePageFinder(PdfPageService pdfService, OpenRouterClient openRouterClient) {
        this.pdfService = pdfService;
        this.openRouterClient = openRouterClient;
    }

    public List<Integer> find(byte[] pdf, int totalPages) throws IOException {
        Set<Integer> found = new TreeSet<>();

        long preparingStarted = System.currentTimeMillis();

        log.info("Preparing PDF chunks. Total pages: {}", totalPages);

        List<PdfChunk> chunks = pdfService.splitForScanning(pdf, CHUNK_PAGES);

        log.info("Prepared {} chunks in {} seconds",
                chunks.size(),
                (System.currentTimeMillis() - preparingStarted) / 1000);

        int chunkNumber = 0;

        for (PdfChunk chunk : chunks) {
            chunkNumber++;

            long scanningStarted = System.currentTimeMillis();

            log.info(
                    "Scanning chunk {}/{}: pages {}-{}, size={} bytes",
                    chunkNumber,
                    chunks.size(),
                    chunk.firstPage(),
                    chunk.lastPage(),
                    chunk.data().length
            );

            PageDetection detection = openRouterClient.analyzePdf(
                    Prompts.FIND_PAGES_SYSTEM,
                    """
                    List the pages containing maintenance schedule information.
                    Use only the red "PDF PAGE n" labels.
                    This section contains original PDF pages %d through %d.
                    """.formatted(chunk.firstPage(), chunk.lastPage()),
                    chunk.data(),
                    PageDetection.class
            );

            log.info(
                    "Finished chunk {}/{} in {} seconds. Detected pages: {}",
                    chunkNumber,
                    chunks.size(),
                    (System.currentTimeMillis() - scanningStarted) / 1000,
                    detection == null ? null : detection.pages()
            );

            if (detection == null || detection.pages() == null) continue;

            for (Integer page : detection.pages()) {
                if (page == null) continue;

                if (page >= chunk.firstPage() && page <= chunk.lastPage()) {
                    found.add(page);
                } else {
                    log.warn(
                            "Ignoring page {} outside chunk range {}-{}",
                            page,
                            chunk.firstPage(),
                            chunk.lastPage()
                    );
                }
            }
        }

        log.info("Pass 1 detected pages: {}", found);

        Set<Integer> expanded = new TreeSet<>();

        for (Integer page : found) {
            for (int nearby = page - MARGIN; nearby <= page + MARGIN; nearby++) {
                if (nearby >= 1 && nearby <= totalPages) expanded.add(nearby);
            }
        }

        if (expanded.size() > MAX_PAGES) {
            log.warn(
                    "Detected {} pages, exceeding warning threshold {}. All detected pages will be analysed.",
                    expanded.size(),
                    MAX_PAGES
            );
        }

        log.info("Pass 1 finished. Selected {} pages for rule extraction", expanded.size());

        return new ArrayList<>(expanded);
    }
}