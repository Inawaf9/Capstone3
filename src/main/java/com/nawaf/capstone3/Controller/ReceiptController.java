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
    public ResponseEntity<?> getReceipts() {
        return ResponseEntity.status(200).body(receiptService.getReceipts());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getReceiptById(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(receiptService.getReceiptById(id));
    }

    @PostMapping("/add/{maintenanceRecordId}")
    public ResponseEntity<?> addReceipt(@PathVariable Integer maintenanceRecordId, @Valid @RequestBody Receipt receipt) {
        receiptService.addReceipt(maintenanceRecordId, receipt);
        return ResponseEntity.status(201).body(new ApiResponse("Receipt added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateReceipt(@PathVariable Integer id, @Valid @RequestBody Receipt receipt) {
        receiptService.updateReceipt(id, receipt);
        return ResponseEntity.status(200).body(new ApiResponse("Receipt updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReceipt(@PathVariable Integer id) {
        receiptService.deleteReceipt(id);
        return ResponseEntity.status(200).body(new ApiResponse("Receipt deleted successfully"));
    }


    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> getAllReceiptForVehicle(@PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(receiptService.getAllReceiptForVehicle(vehicleId));
    }


    @GetMapping("/vehicle/{vehicleId}/total")
    public ResponseEntity<?>getTotalReceiptAmountByVehicleId(@PathVariable Integer vehicleId){
        return ResponseEntity.status(200).body(receiptService.getTotalReceiptAmountByVehicleId(vehicleId));
    }

}