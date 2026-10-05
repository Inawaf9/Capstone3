package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.Receipt;
import com.nawaf.capstone3.Repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;


    public List<Receipt> getAllReceipts(){
        List<Receipt>  receipts = receiptRepository.findAll();

        if(receipts.isEmpty()) throw new ApiException("Receipts not found");

        return receipts;
    }

    public Receipt getReceiptById(Integer id){
        Receipt receipt = receiptRepository.findReceiptById(id);

        if(receipt == null) throw new ApiException("Receipt not found");

        return receipt;
    }

    public void createReceipt(Receipt receipt){
        receiptRepository.save(receipt);
    }

    public void updateReceipt(Integer id, Receipt receipt) {
        Receipt oldReceipt = getReceiptById(id);

        oldReceipt.setFileUrl(receipt.getFileUrl());
        oldReceipt.setTotalAmount(receipt.getTotalAmount());
        oldReceipt.setExtractedDate(receipt.getExtractedDate());

        receiptRepository.save(oldReceipt);
    }

    public void deleteReceipt(Integer id){
        Receipt receipt = getReceiptById(id);

        receiptRepository.delete(receipt);
    }
}
