package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.Receipt;
import com.nawaf.capstone3.Service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;


    @GetMapping("/get-all")
    public ResponseEntity<?> getAllReceipts(){
        return ResponseEntity.status(200).body(receiptService.getAllReceipts());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getReceiptById(@PathVariable Integer id){
        return ResponseEntity.status(200).body(receiptService.getReceiptById(id));
    }

    @PostMapping("/add")
    public ResponseEntity<?> createReceipt(@Valid @RequestBody Receipt receipt){
        receiptService.createReceipt(receipt);

        return ResponseEntity.status(201).body(new ApiResponse("Create receipt successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateReceipt(@PathVariable Integer id, @Valid @RequestBody Receipt receipt){
        receiptService.updateReceipt(id, receipt);

        return ResponseEntity.status(200).body(new ApiResponse("Update receipt successfully"));
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<?> deleteReceipt(@PathVariable Integer id){
        receiptService.deleteReceipt(id);

        return ResponseEntity.status(200).body(new ApiResponse("Delete receipt successfully"));
    }
}
