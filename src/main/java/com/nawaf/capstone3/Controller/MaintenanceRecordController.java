package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.DTO.CompleteMaintenanceDTO;
import com.nawaf.capstone3.DTO.UpdateCostDTO;
import com.nawaf.capstone3.DTO.UpdateNoteDTO;
import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Service.MaintenanceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/maintenance-record", "/api/v1/maintenanceRecord"})
@RequiredArgsConstructor
public class MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;

    @GetMapping("/get-Due-Maintenances/{vehicleId}")
    public ResponseEntity<?> getDueMaintenances(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(maintenanceRecordService.getDueMaintenances(vehicleId));
    }

    @GetMapping("/get-Over-due-Maintenances/{vehicleId}")
    public ResponseEntity<?> getOverdueMaintenances(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(maintenanceRecordService.getOverdueMaintenances(vehicleId));
    }

    @GetMapping("/get-Upcoming-Maintenance/{vehicleId}")
    public ResponseEntity<?> getUpcomingMaintenance(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(maintenanceRecordService.getUpcomingMaintenance(vehicleId));
    }

    @PostMapping("/complet-Maintenance/{vehicleId}/{maintenanceRuleId}")
    public ResponseEntity<?> completeMaintenance(@PathVariable Integer vehicleId,
            @PathVariable Integer maintenanceRuleId, @Valid @RequestBody CompleteMaintenanceDTO input) {
        return ResponseEntity.status(201).body(maintenanceRecordService.completeMaintenance(vehicleId, maintenanceRuleId, input));
    }

    @GetMapping("/get-Vehicle-Maintenance-History/{vehicleId}")
    public ResponseEntity<?> getVehicleMaintenanceHistory(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(maintenanceRecordService.getVehicleMaintenanceHistory(vehicleId));
    }

    @PutMapping("/update-note/{maintenanceRecordId}")
    public ResponseEntity<?> updateNoteByRecordId(@PathVariable Integer maintenanceRecordId,
            @Valid @RequestBody UpdateNoteDTO input) {
        return ResponseEntity.ok(maintenanceRecordService.updateNoteByRecordId(maintenanceRecordId, input));
    }

    @GetMapping("/get-records-by-service-name/{serviceName}")
    public ResponseEntity<?> getRecordsByServiceName(@PathVariable String serviceName) {
        return ResponseEntity.ok(maintenanceRecordService.getRecordsByServiceName(serviceName));
    }

    @GetMapping("/get-records-by-vehicleId-and-workshop/{vehicleId}/{workshop}")
    public ResponseEntity<?> getRecordsByVehicleIdAndWorkshop(@PathVariable Integer vehicleId, @PathVariable String workshop) {
        return ResponseEntity.ok(maintenanceRecordService.getRecordsByVehicleIdAndWorkshop(vehicleId, workshop));
    }

    @PutMapping("/update-cost/{maintenanceRecordId}")
    public ResponseEntity<?> updateCostByRecordId(@PathVariable Integer maintenanceRecordId,
            @Valid @RequestBody UpdateCostDTO input) {
        return ResponseEntity.ok(maintenanceRecordService.updateCostByRecordId(maintenanceRecordId, input.cost()));
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getMaintenanceRecords() {
        return ResponseEntity.ok(maintenanceRecordService.getMaintenanceRecords());
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
