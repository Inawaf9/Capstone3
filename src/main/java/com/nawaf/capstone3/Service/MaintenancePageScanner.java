package com.nawaf.capstone3.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class MaintenancePageScanner {

    private static final int MARGIN = 1;

    private static final List<String> MAINTENANCE_KEYWORDS = List.of(
            "maintenance schedule",
            "scheduled maintenance",
            "maintenance interval",
            "service schedule",
            "service interval",
            "maintenance",
            "replace",
            "replacement",
            "inspect",
            "inspection",
            "kilometer",
            "kilometre",
            "miles",
            "months",
            "severe conditions",
            "severe driving"
    );

    private final PdfPageService pdfPageService;

    public List<Integer> findCandidatePages(byte[] pdf) throws IOException {
        List<String> pageTexts = pdfPageService.extractAllPageTexts(pdf);
        Set<Integer> detectedPages = new TreeSet<>();

        for (int index = 0; index < pageTexts.size(); index++) {
            String text = pageTexts.get(index);

            if (isMaintenancePage(text)) detectedPages.add(index + 1);
        }

        return expandPages(detectedPages, pageTexts.size());
    }

    private boolean isMaintenancePage(String text) {
        if (text == null || text.isBlank()) return false;

        String normalizedText = text.toLowerCase(Locale.ROOT);
        int matches = 0;

        for (String keyword : MAINTENANCE_KEYWORDS) {
            if (normalizedText.contains(keyword)) matches++;
        }

        return matches >= 2;
    }

    private List<Integer> expandPages(Set<Integer> detectedPages, int totalPages) {
        Set<Integer> expandedPages = new TreeSet<>();

        for (Integer page : detectedPages) {
            for (int nearby = page - MARGIN; nearby <= page + MARGIN; nearby++) {
                if (nearby >= 1 && nearby <= totalPages) expandedPages.add(nearby);
            }
        }

        return new ArrayList<>(expandedPages);
    }
}