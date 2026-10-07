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

        for (PdfChunk chunk : pdfService.splitForScanning(pdf, CHUNK_PAGES)) {
            PageDetection d = gemini.extract(
                    Prompts.FIND_PAGES_SYSTEM,
                    "List the PDF page numbers in this section that contain maintenance schedule information.",
                    chunk.data(),
                    PageDetection.class);

            if (d == null || d.pages() == null) continue;
            for (Integer p : d.pages()) {
                // نتجاهل أي رقم خارج نطاق هذا الجزء (النموذج قد يخطئ)
                if (p != null && p >= chunk.firstPage() && p <= chunk.lastPage()) found.add(p);
            }
        }
        log.info("Pass 1 detected pages: {}", found);

        Set<Integer> expanded = new TreeSet<>();
        for (int p : found) {
            for (int q = p - MARGIN; q <= p + MARGIN; q++) {
                if (q >= 1 && q <= totalPages) expanded.add(q);
            }
        }

        if (expanded.size() > MAX_PAGES) {
            log.warn("Too many pages detected ({}), truncating to {}", expanded.size(), MAX_PAGES);
            return expanded.stream().limit(MAX_PAGES).toList();
        }
        return new ArrayList<>(expanded);
    }


}