package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Service.MaintenanceRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maintenance-rule")
@RequiredArgsConstructor
public class MaintenanceRuleController {
    private final MaintenanceRuleService maintenanceRuleService;

    @GetMapping("/get-all")
<<<<<<< Updated upstream
    public ResponseEntity<?> getMaintenanceRules() {
        return ResponseEntity.status(200).body(maintenanceRuleService.getMaintenanceRules());
=======
    public ResponseEntity<?>getAll() {
        List<MaintenanceRule> maintenanceRuleList = maintenanceRuleService.getAll();
        return ResponseEntity.status(200).body(maintenanceRuleList);
    }
    @GetMapping("/get/{maintenanceRuleId}")
    public ResponseEntity<?>getMaintenanceRuleById(@PathVariable Integer maintenanceRuleId) {
       MaintenanceRule maintenanceRule=maintenanceRuleService.getMaintenanceRuleById(maintenanceRuleId);
       return ResponseEntity.status(200).body(maintenanceRule);
    }

    @PostMapping("/add")
    public ResponseEntity<?>addMaintenanceRule(@RequestBody MaintenanceRule maintenanceRule){
        maintenanceRuleService.addMaintenanceRule(maintenanceRule);
        return ResponseEntity.status(200).body(new ApiResponse("added successfully"));
    }
    @PutMapping("/update/{maintenanceRuleId}")
    public ResponseEntity<?>updateMaintenanceRule(@PathVariable Integer maintenanceRuleId,@RequestBody MaintenanceRule maintenanceRule){
        maintenanceRuleService.updateMaintenanceRule(maintenanceRuleId, maintenanceRule);
        return ResponseEntity.status(200).body(new ApiResponse("updated successfully"));
>>>>>>> Stashed changes
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getMaintenanceRuleById(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(maintenanceRuleService.getMaintenanceRuleById(id));
    }

    @PostMapping("/add/{vehicleId}")
    public ResponseEntity<?> addMaintenanceRule(@PathVariable Integer vehicleId, @Valid @RequestBody MaintenanceRule maintenanceRule, Errors errors) {
        maintenanceRuleService.addMaintenanceRule(vehicleId, maintenanceRule);
        return ResponseEntity.status(201).body(new ApiResponse("Maintenance rule added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMaintenanceRule(@PathVariable Integer id, @Valid @RequestBody MaintenanceRule maintenanceRule, Errors errors) {
        maintenanceRuleService.updateMaintenanceRule(id, maintenanceRule);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance rule updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMaintenanceRule(@PathVariable Integer id) {
        maintenanceRuleService.deleteMaintenanceRule(id);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance rule deleted successfully"));
    }
}
