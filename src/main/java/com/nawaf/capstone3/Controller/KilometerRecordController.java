package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Model.KilometerRecord;
import com.nawaf.capstone3.Service.KilometerRecordService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/kilometer-record")
@AllArgsConstructor
public class KilometerRecordController {

    private final KilometerRecordService kilometerRecordService;


    // Get all kilometer records
    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.status(200).body(kilometerRecordService.get());
    }


    // Add kilometer record to a vehicle
    @PostMapping("/add/{userId}/{vehicleId}")
    public ResponseEntity<?> add(
            @PathVariable Integer userId,
            @PathVariable Integer vehicleId,
            @Valid @RequestBody KilometerRecord kilometerRecord) {

        kilometerRecordService.add(userId, vehicleId, kilometerRecord);
        return ResponseEntity.status(200).body(" KilometerRecord Add successfully");
    }


        @PutMapping("/update/{userId}/{vehicleId}/{kilometerRecordId}")
    public ResponseEntity<?> update(
            @PathVariable Integer userId,
            @PathVariable Integer vehicleId,
            @PathVariable Integer kilometerRecordId,
            @Valid @RequestBody KilometerRecord updateKilometerRecord) {

        kilometerRecordService.update(
                userId,
                vehicleId,
                kilometerRecordId,
                updateKilometerRecord
        );

        return ResponseEntity.status(200).body("kilometerRecord update successfully");
    }


    @DeleteMapping("/delete/{userId}/{vehicleId}/{kilometerRecordId}")
    public ResponseEntity<?> delete(@PathVariable Integer userId, @PathVariable Integer vehicleId, @PathVariable Integer kilometerRecordId) {

        kilometerRecordService.delete(userId,vehicleId,kilometerRecordId);
        return ResponseEntity.status(200).body(" kilometerRecord delete successfully");
    }
}