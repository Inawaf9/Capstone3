package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Model.KilometerRecord;
import com.nawaf.capstone3.Service.KilometerRecordService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/add/{vehicleId}")
    public ResponseEntity<?> add(@PathVariable Integer vehicleId, @Valid @RequestBody KilometerRecord kilometerRecord) {

        kilometerRecordService.add(vehicleId, kilometerRecord);

        return ResponseEntity.status(200).body("add successfully");
    }


    @PutMapping("/update/{kilometerRecordId}")
    public ResponseEntity<?> update(@PathVariable Integer kilometerRecordId, @Valid @RequestBody KilometerRecord updateKilometerRecord) {

        kilometerRecordService.update(kilometerRecordId, updateKilometerRecord);
        return ResponseEntity.ok().body("update successfully");
    }


    @DeleteMapping("/delete/{kilometerRecordId}")
    public ResponseEntity<?> delete(@PathVariable Integer kilometerRecordId) {

        kilometerRecordService.delete(kilometerRecordId);

        return ResponseEntity.status(200).body(" delete successfully");
    }
}