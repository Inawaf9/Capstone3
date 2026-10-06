package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Service.MaintenanceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maintenance-record")
@RequiredArgsConstructor
public class MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getMaintenanceRecords() {
        return ResponseEntity.status(200).body(maintenanceRecordService.getMaintenanceRecords());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getMaintenanceRecordById(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(maintenanceRecordService.getMaintenanceRecordById(id));
    }

    @PostMapping("/add/{vehicleId}/{maintenanceRuleId}")
    public ResponseEntity<?> addMaintenanceRecord(@PathVariable Integer vehicleId, @PathVariable Integer maintenanceRuleId, @Valid @RequestBody MaintenanceRecord maintenanceRecord) {
        maintenanceRecordService.addMaintenanceRecord(vehicleId, maintenanceRuleId, maintenanceRecord);
        return ResponseEntity.status(201).body(new ApiResponse("Maintenance record added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMaintenanceRecord(@PathVariable Integer id, @Valid @RequestBody MaintenanceRecord maintenanceRecord) {
        maintenanceRecordService.updateMaintenanceRecord(id, maintenanceRecord);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance record updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMaintenanceRecord(@PathVariable Integer id) {
        maintenanceRecordService.deleteMaintenanceRecord(id);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance record deleted successfully"));
    }

    @GetMapping("/cost/{vehicleId}/{year}")
    public ResponseEntity<?>getMaintenanceCostByYear(@PathVariable Integer vehicleId ,@PathVariable Integer year){
        return ResponseEntity.status(200).body(maintenanceRecordService.getMaintenanceCostByYear(vehicleId,year));
    }
}