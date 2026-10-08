package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Client.OpenRouterClient;
import com.nawaf.capstone3.DTO.ReceiptDTO;
import com.nawaf.capstone3.Model.Receipt;
import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;
import com.nawaf.capstone3.Repository.ReceiptRepository;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
public class ReceiptAiService {
    private final OpenRouterClient openRouterClient;
    private final ReceiptRepository receiptRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final Validator validator;
    private final TransactionTemplate transactions;

    public ReceiptDTO analyzeAndSaveReceipt(Integer maintenanceRecordId, MultipartFile image) {
        validateImage(image);
        if (!maintenanceRecordRepository.existsById(maintenanceRecordId))
            throw new ApiException("Maintenance record not found");
        ReceiptDTO dto = openRouterClient.analyzeImage(image, """
                Extract totalAmount (finite nonnegative number), extractedDate (YYYY-MM-DD),
                and services (array of service-name strings). Use null for unreadable required values.
                Do not invent values. Services are an extraction preview, not new maintenance rules.
                """, ReceiptDTO.class);
        if (dto == null || !validator.validate(dto).isEmpty() || dto.getTotalAmount() == null
                || !Double.isFinite(dto.getTotalAmount()))
            throw new ApiException("Receipt extraction is incomplete or invalid");
        transactions.executeWithoutResult(status -> {
            var record = maintenanceRecordRepository.findMaintenanceRecordById(maintenanceRecordId);
            if (record == null) throw new ApiException("Maintenance record no longer exists");
            Receipt receipt = new Receipt();
            receipt.setTotalAmount(dto.getTotalAmount());
            receipt.setExtractedDate(dto.getExtractedDate());
            receipt.setMaintenanceRecord(record);
            receiptRepository.saveAndFlush(receipt);
        });
        return dto;
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) throw new ApiException("Receipt image is required");
        if (image.getSize() > 5 * 1024 * 1024) throw new ApiException("Receipt image must not exceed 5 MB");
        if (!"image/jpeg".equals(image.getContentType()) && !"image/png".equals(image.getContentType()))
            throw new ApiException("Only JPEG and PNG receipt images are supported");
        try (var stream = image.getInputStream(); var input = ImageIO.createImageInputStream(stream)) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new ApiException("Invalid image content");
            var reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName();
                boolean matches = "image/png".equals(image.getContentType()) ? "png".equalsIgnoreCase(format)
                        : "jpeg".equalsIgnoreCase(format);
                if (!matches || (long) reader.getWidth(0) * reader.getHeight(0) > 20_000_000)
                    throw new ApiException("Image type or dimensions are invalid");
                if (reader.read(0) == null) throw new ApiException("Unreadable image content");
            } finally { reader.dispose(); }
        } catch (java.io.IOException | IllegalArgumentException exception) {
            throw new ApiException("Invalid image content");
        }
    }
}
