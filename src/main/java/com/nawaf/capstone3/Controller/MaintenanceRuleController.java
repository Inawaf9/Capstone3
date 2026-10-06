package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Service.MaintenanceRuleService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/maintenanceRule")
public class MaintenanceRuleController {
    private final MaintenanceRuleService maintenanceRuleService;

    @GetMapping("/get-all")
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
    }


    @DeleteMapping("/delete/{maintenanceRuleId}")
    public ResponseEntity<?>deleteMaintenanceRule(@PathVariable  Integer maintenanceRuleId){
        maintenanceRuleService.deleteMaintenanceRule(maintenanceRuleId);
        return ResponseEntity.status(200).body(new ApiResponse("deleted successfully"));
    }


}