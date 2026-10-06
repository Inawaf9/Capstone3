package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Service.MaintenanceRecordService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/maintenanceRecord")
public class MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;

    @GetMapping("/get")
    public ResponseEntity<?> getAll(){
        List<MaintenanceRecord> maintenanceRecordList=maintenanceRecordService.getAll();
        return ResponseEntity.status(200).body(maintenanceRecordList);
    }

    @PostMapping("/add")
    public ResponseEntity<?>addMaintenanceRecord(@RequestBody MaintenanceRecord maintenanceRecord){
        maintenanceRecordService.addMaintenanceRecord(maintenanceRecord);
        return ResponseEntity.status(200).body(new ApiResponse("added successfully"));
    }
    @DeleteMapping("/delete/{maintenanceRecordId}")
    public ResponseEntity<?>deleteMaintenanceRecord(@PathVariable Integer maintenanceRecordId){
        maintenanceRecordService.deleteMaintenanceRecord(maintenanceRecordId);
        return ResponseEntity.status(200).body(new ApiResponse("deleted done "));
    }
    @PutMapping("/update/{maintenanceRecordId}")
    public ResponseEntity<?>updatedMaintenanceRecord(@PathVariable Integer maintenanceRecordId,@RequestBody MaintenanceRecord maintenanceRecord){
        maintenanceRecordService.updateMaintenanceRecord(maintenanceRecordId,maintenanceRecord);
        return ResponseEntity.status(200).body(new ApiResponse("updated done "));
    }
}