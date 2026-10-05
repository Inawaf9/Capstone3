package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Model.Receipt;
import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;
import com.nawaf.capstone3.Repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAll();
    }

    public Receipt getReceiptById(Integer id) {
        Receipt receipt = receiptRepository.findReceiptById(id);

        if (receipt == null) throw new ApiException("Receipt not found");

        return receipt;
    }

    public void addReceipt(Integer maintenanceRecordId, Receipt receipt) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(maintenanceRecordId);

        if (maintenanceRecord == null) throw new ApiException("Maintenance record not found");

        receipt.setMaintenanceRecord(maintenanceRecord);

        receiptRepository.save(receipt);
    }

    public void updateReceipt(Integer id, Receipt receipt) {
        Receipt oldReceipt = getReceiptById(id);

        oldReceipt.setFileUrl(receipt.getFileUrl());
        oldReceipt.setTotalAmount(receipt.getTotalAmount());
        oldReceipt.setExtractedDate(receipt.getExtractedDate());

        receiptRepository.save(oldReceipt);
    }

    public void deleteReceipt(Integer id) {
        Receipt receipt = getReceiptById(id);

        receiptRepository.delete(receipt);
    }
}