package com.nawaf.capstone3.Service;
import com.nawaf.capstone3.DTO.PdfChunk;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class PdfPageService {

        public int pageCount(byte[] pdf) throws IOException {
            try (PDDocument d = Loader.loadPDF(pdf)) {
                return d.getNumberOfPages();
            }
        }

        /** يقسم الملف إلى أجزاء متتابعة ويكتب رقم الصفحة الفعلي على كل صفحة (للمرور الأول فقط) */
        public List<PdfChunk> splitForScanning(byte[] pdf, int pagesPerChunk) throws IOException {
            List<PdfChunk> chunks = new ArrayList<>();
            try (PDDocument source = Loader.loadPDF(pdf)) {
                int total = source.getNumberOfPages();
                for (int start = 0; start < total; start += pagesPerChunk) {
                    int end = Math.min(start + pagesPerChunk, total);
                    try (PDDocument part = new PDDocument();
                         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        for (int i = start; i < end; i++) {
                            PDPage imported = part.importPage(source.getPage(i));
                            stamp(part, imported, i + 1);
                        }
                        part.save(out);
                        chunks.add(new PdfChunk(start + 1, end, out.toByteArray()));
                    }
                }
            }
            return chunks;
        }

        /** يقتطع صفحات محددة (الترقيم يبدأ من 1) من الملف الأصلي النظيف بدون كتابة أرقام */
        public byte[] extractPages(byte[] pdf, List<Integer> pages) throws IOException {
            try (PDDocument source = Loader.loadPDF(pdf);
                 PDDocument part = new PDDocument();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                for (int p : pages) {
                    part.importPage(source.getPage(p - 1));
                }
                part.save(out);
                return out.toByteArray();
            }
        }

        private void stamp(PDDocument doc, PDPage page, int number) throws IOException {
            PDRectangle box = page.getMediaBox();
            try (PDPageContentStream cs = new PDPageContentStream(
                    doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.setNonStrokingColor(1f, 0f, 0f);   // أحمر
                cs.newLineAtOffset(box.getLowerLeftX() + 12, box.getUpperRightY() - 24);
                cs.showText("PDF PAGE " + number);
                cs.endText();
            }
        }

}