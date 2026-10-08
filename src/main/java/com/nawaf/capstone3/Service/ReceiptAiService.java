package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Client.OpenRouterClient;
import com.nawaf.capstone3.DTO.ReceiptDTO;
import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Model.Receipt;
import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;
import com.nawaf.capstone3.Repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReceiptAiService {

    private final OpenRouterClient openRouterClient;
    private final ReceiptRepository receiptRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public ReceiptDTO analyzeAndSaveReceipt(Integer maintenanceRecordId, MultipartFile image) {

        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Receipt image is required");
        }

        MaintenanceRecord maintenanceRecord =
                maintenanceRecordRepository
                        .findMaintenanceRecordById(maintenanceRecordId);

        if (maintenanceRecord == null) {
            throw new RuntimeException("Maintenance record not found");
        }

        String prompt = """
                Analyze this receipt image.

                Extract the following information:

                1. totalAmount:
                   The total amount paid on the receipt.

                2. extractedDate:
                   The date shown on the receipt.
                   Return it in YYYY-MM-DD format.

                3. serviceType:
                   The type of maintenance or service shown on the receipt.

                Return only the structured result.
                """;

        ReceiptDTO receiptDTO = openRouterClient.analyzeImage(
                image,
                prompt,
                ReceiptDTO.class
        );

        Receipt receipt = new Receipt();

        receipt.setTotalAmount(receiptDTO.getTotalAmount());
        receipt.setExtractedDate(receiptDTO.getExtractedDate());
        receipt.setMaintenanceRecord(maintenanceRecord);

        receiptRepository.save(receipt);

        return receiptDTO;
    }
}