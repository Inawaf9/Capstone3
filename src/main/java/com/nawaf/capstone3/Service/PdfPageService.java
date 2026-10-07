package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.DTO.PdfChunk;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfPageService {

    public int pageCount(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return document.getNumberOfPages();
        }
    }

    public String extractPageText(byte[] pdf, int page) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(page);
            stripper.setEndPage(page);

            return stripper.getText(document);
        }
    }

    public List<PdfChunk> splitForScanning(byte[] pdf, int pagesPerChunk) throws IOException {
        List<PdfChunk> chunks = new ArrayList<>();

        try (PDDocument source = Loader.loadPDF(pdf)) {
            int totalPages = source.getNumberOfPages();

            for (int start = 0; start < totalPages; start += pagesPerChunk) {
                int end = Math.min(start + pagesPerChunk, totalPages);

                try (PDDocument part = new PDDocument();
                     ByteArrayOutputStream output = new ByteArrayOutputStream()) {

                    for (int page = start; page < end; page++) {
                        PDPage imported = part.importPage(source.getPage(page));
                        stamp(part, imported, page + 1);
                    }

                    part.save(output);
                    chunks.add(new PdfChunk(start + 1, end, output.toByteArray()));
                }
            }
        }

        return chunks;
    }

    public byte[] extractPages(byte[] pdf, List<Integer> pages) throws IOException {
        try (PDDocument source = Loader.loadPDF(pdf);
             PDDocument part = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            int totalPages = source.getNumberOfPages();

            for (Integer page : pages) {
                if (page == null || page < 1 || page > totalPages) continue;

                part.importPage(source.getPage(page - 1));
            }

            part.save(output);

            return output.toByteArray();
        }
    }

    private void stamp(PDDocument document, PDPage page, int pageNumber) throws IOException {
        PDRectangle box = page.getMediaBox();

        try (PDPageContentStream contentStream = new PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true)) {

            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            contentStream.setNonStrokingColor(1f, 0f, 0f);
            contentStream.newLineAtOffset(box.getLowerLeftX() + 12, box.getUpperRightY() - 24);
            contentStream.showText("PDF PAGE " + pageNumber);
            contentStream.endText();
        }
    }

    public List<String> extractAllPageTexts(byte[] pdf) throws IOException {
        List<String> texts = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();

            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                texts.add(stripper.getText(document));
            }
        }

        return texts;
    }
}