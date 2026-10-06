package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Service.MaintenanceRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maintenance-rule")
@RequiredArgsConstructor
public class MaintenanceRuleController {

    private final MaintenanceRuleService maintenanceRuleService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getMaintenanceRules() {
        return ResponseEntity.status(200).body(maintenanceRuleService.getMaintenanceRules());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getMaintenanceRuleById(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(maintenanceRuleService.getMaintenanceRuleById(id));
    }

    @PostMapping("/add/{userManualId}")
    public ResponseEntity<?> addMaintenanceRule(@PathVariable Integer userManualId, @Valid @RequestBody MaintenanceRule maintenanceRule) {
        maintenanceRuleService.addMaintenanceRule(userManualId, maintenanceRule);
        return ResponseEntity.status(201).body(new ApiResponse("Maintenance rule added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMaintenanceRule(@PathVariable Integer id, @Valid @RequestBody MaintenanceRule maintenanceRule) {
        maintenanceRuleService.updateMaintenanceRule(id, maintenanceRule);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance rule updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMaintenanceRule(@PathVariable Integer id) {
        maintenanceRuleService.deleteMaintenanceRule(id);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance rule deleted successfully"));
    }
}