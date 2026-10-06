package com.nawaf.capstone3.Service;
import com.nawaf.capstone3.Service.GeminiCaller;
import com.nawaf.capstone3.Service.Prompts;
import com.nawaf.capstone3.DTO.PageDetection;
import com.nawaf.capstone3.DTO.PdfChunk;
import com.nawaf.capstone3.Service.PdfPageService;
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

    private static final int CHUNK_PAGES = 25;  // صغّره إلى 15 إذا تجاوز الجزء ~14MB
    private static final int MARGIN = 1;        // صفحات إضافية قبل وبعد كل صفحة مكتشفة
    private static final int MAX_PAGES = 80;    // حد أمان

    private final PdfPageService pdfService;
    private final GeminiCaller gemini;

    public MaintenancePageFinder(PdfPageService pdfService, GeminiCaller gemini) {
        this.pdfService = pdfService;
        this.gemini = gemini;
    }
    public List<Integer> find(byte[] pdf, int totalPages) throws IOException {
        Set<Integer> found = new TreeSet<>();

        // جديد: نعرف متى بدأ تجهيز أجزاء الملف
        long preparingStarted = System.currentTimeMillis();

        log.info("Preparing PDF chunks. Total pages: {}", totalPages);

        List<PdfChunk> chunks =
                pdfService.splitForScanning(pdf, CHUNK_PAGES);

        log.info("Prepared {} chunks in {} seconds",
                chunks.size(),
                (System.currentTimeMillis() - preparingStarted) / 1000);

        int chunkNumber = 0;

        for (PdfChunk chunk : chunks) {
            chunkNumber++;

            long scanningStarted = System.currentTimeMillis();

            // جديد: إظهار الجزء الجاري تحليله وحجمه
            log.info(
                    "Scanning chunk {}/{}: pages {}-{}, size={} bytes",
                    chunkNumber,
                    chunks.size(),
                    chunk.firstPage(),
                    chunk.lastPage(),
                    chunk.data().length
            );

            PageDetection detection = gemini.extract(
                    Prompts.FIND_PAGES_SYSTEM,
                    """
                    List the pages containing maintenance schedule information.
                    Use only the red "PDF PAGE n" labels.
                    This section contains original PDF pages %d through %d.
                    """.formatted(chunk.firstPage(), chunk.lastPage()),
                    chunk.data(),
                    PageDetection.class
            );

            log.info("Finished chunk {}/{} in {} seconds. Detected pages: {}",
                    chunkNumber,
                    chunks.size(),
                    (System.currentTimeMillis() - scanningStarted) / 1000,
                    detection == null ? null : detection.pages());

            if (detection == null || detection.pages() == null) {
                continue;
            }

            for (Integer page : detection.pages()) {
                if (page == null) {
                    continue;
                }

                // الرقم أصلي بالفعل لأن PdfPageService يكتبه على الصفحة
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

        for (int page : found) {
            for (int nearby = page - MARGIN;
                 nearby <= page + MARGIN;
                 nearby++) {

                if (nearby >= 1 && nearby <= totalPages) {
                    expanded.add(nearby);
                }
            }
        }

        // تعديل: التحذير بدل إسقاط الصفحات بعد أول 80 صفحة
        if (expanded.size() > MAX_PAGES) {
            log.warn(
                    "Detected {} pages, exceeding warning threshold {}. "
                            + "All detected pages will be analysed.",
                    expanded.size(),
                    MAX_PAGES
            );
        }

        log.info("Pass 1 finished. Selected {} pages for rule extraction",
                expanded.size());

        return new ArrayList<>(expanded);
    }

//    public List<Integer> find(byte[] pdf, int totalPages) throws IOException {
//        Set<Integer> found = new TreeSet<>();
//
//        for (PdfChunk chunk : pdfService.splitForScanning(pdf, CHUNK_PAGES)) {
//            PageDetection d = gemini.extract(
//                    Prompts.FIND_PAGES_SYSTEM,
//                    "List the PDF page numbers in this section that contain maintenance schedule information.",
//                    chunk.data(),
//                    PageDetection.class);
//
//            if (d == null || d.pages() == null) continue;
//            for (Integer p : d.pages()) {
//                // نتجاهل أي رقم خارج نطاق هذا الجزء (النموذج قد يخطئ)
//                if (p != null && p >= chunk.firstPage() && p <= chunk.lastPage()) found.add(p);
//            }
//        }
//        log.info("Pass 1 detected pages: {}", found);
//
//        Set<Integer> expanded = new TreeSet<>();
//        for (int p : found) {
//            for (int q = p - MARGIN; q <= p + MARGIN; q++) {
//                if (q >= 1 && q <= totalPages) expanded.add(q);
//            }
//        }
//
//        if (expanded.size() > MAX_PAGES) {
//            log.warn("Too many pages detected ({}), truncating to {}", expanded.size(), MAX_PAGES);
//            return expanded.stream().limit(MAX_PAGES).toList();
//        }
//        return new ArrayList<>(expanded);
//    }


}