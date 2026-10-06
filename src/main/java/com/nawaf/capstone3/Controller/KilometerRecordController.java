package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.DTO.KilometerRecordDTO;
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

    @GetMapping("/get-all")
    public ResponseEntity<?> getKilometerRecords() {
        return ResponseEntity.status(200).body(kilometerRecordService.getKilometerRecords());
    }

    @GetMapping("/get/{userId}/{vehicleId}/{id}")
    public ResponseEntity<?> getKilometerRecordById(@PathVariable Integer userId, @PathVariable Integer vehicleId, @PathVariable Integer id) {
        return ResponseEntity.status(200).body(kilometerRecordService.getKilometerRecordById(userId, vehicleId, id));
    }

    @PostMapping("/add/{userId}/{vehicleId}")
    public ResponseEntity<?> addKilometerRecord(@PathVariable Integer userId, @PathVariable Integer vehicleId, @Valid @RequestBody KilometerRecord kilometerRecord) {
        kilometerRecordService.addKilometerRecord(userId, vehicleId, kilometerRecord);
        return ResponseEntity.status(201).body(new ApiResponse("Kilometer record added successfully"));
    }

    @PutMapping("/update/{userId}/{vehicleId}/{id}")
    public ResponseEntity<?> updateKilometerRecord(@PathVariable Integer userId, @PathVariable Integer vehicleId, @PathVariable Integer id, @Valid @RequestBody KilometerRecord kilometerRecord) {
        kilometerRecordService.updateKilometerRecord(userId, vehicleId, id, kilometerRecord);
        return ResponseEntity.status(200).body(new ApiResponse("Kilometer record updated successfully"));
    }

    @DeleteMapping("/delete/{userId}/{vehicleId}/{id}")
    public ResponseEntity<?> deleteKilometerRecord(@PathVariable Integer userId, @PathVariable Integer vehicleId, @PathVariable Integer id) {
        kilometerRecordService.deleteKilometerRecord(userId, vehicleId, id);
        return ResponseEntity.status(200).body(new ApiResponse("Kilometer record deleted successfully"));
    }

    @PostMapping("/{userId}/{vehicleId}/record")
    public ResponseEntity<?> recordKilometers(@PathVariable Integer userId, @PathVariable Integer vehicleId, @Valid @RequestBody KilometerRecordDTO dto) {
        kilometerRecordService.recordKilometers(userId, vehicleId, dto);
        return ResponseEntity.status(201).body(new ApiResponse("Kilometer reading recorded successfully"));
    }

    @GetMapping("/{userId}/{vehicleId}/latest")
    public ResponseEntity<?> getLatestKilometerRecord(@PathVariable Integer userId, @PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(kilometerRecordService.getLatestKilometerRecord(userId, vehicleId));
    }

    @GetMapping("/{userId}/{vehicleId}/history")
    public ResponseEntity<?> getKilometerHistory(@PathVariable Integer userId, @PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(kilometerRecordService.getKilometerHistory(userId, vehicleId));
    }

    @GetMapping("/{userId}/{vehicleId}/average-monthly")
    public ResponseEntity<?> getAverageMonthlyKilometers(@PathVariable Integer userId, @PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(kilometerRecordService.getAverageMonthlyKilometers(userId, vehicleId));
    }

    @GetMapping("/{userId}/{vehicleId}/distance-traveled")
    public ResponseEntity<?> getDistanceTraveled(@PathVariable Integer userId, @PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(kilometerRecordService.getDistanceTraveled(userId, vehicleId));
    }

    @GetMapping("/{userId}/{vehicleId}/monthly-distance")
    public ResponseEntity<?> getMonthlyDistance(@PathVariable Integer userId, @PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(kilometerRecordService.getMonthlyDistance(userId, vehicleId));
    }
}