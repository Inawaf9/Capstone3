package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.DTO.ReceiptDTO;
import com.nawaf.capstone3.Model.Receipt;
import com.nawaf.capstone3.Service.ReceiptAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/receipt-ai")
@RequiredArgsConstructor
public class ReceiptAiController {

    private final ReceiptAiService receiptAiService;

    @PostMapping("/add/{maintenanceRecordId}")
    public ResponseEntity<ReceiptDTO> addReceiptFromImage(@PathVariable Integer maintenanceRecordId, @RequestParam("image") MultipartFile image) {
        ReceiptDTO receiptDTO = receiptAiService.analyzeAndSaveReceipt(maintenanceRecordId, image);
        return ResponseEntity.ok(receiptDTO);
    }
}