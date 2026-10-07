package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.UserManual;
import com.nawaf.capstone3.Service.ManualAnalysisService;
import com.nawaf.capstone3.Service.UserManualService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-manual")
@AllArgsConstructor
public class UserManualController {

    private final UserManualService userManualService;
    private final ManualAnalysisService manualAnalysisService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getUserManuals() {
        return ResponseEntity.status(200).body(userManualService.getUserManuals());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getUserManualById(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(userManualService.getUserManualById(id));
    }

    @PostMapping("/add/{vehicleId}")
    public ResponseEntity<?> addUserManual(@PathVariable Integer vehicleId, @Valid @RequestBody UserManual userManual) {
        userManualService.addUserManual(vehicleId, userManual);
        return ResponseEntity.status(201).body(new ApiResponse("User manual added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUserManual(@PathVariable Integer id, @Valid @RequestBody UserManual userManual) {
        userManualService.updateUserManual(id, userManual);
        return ResponseEntity.status(200).body(new ApiResponse("User manual updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUserManual(@PathVariable Integer id) {
        userManualService.deleteUserManual(id);
        return ResponseEntity.status(200).body(new ApiResponse("User manual deleted successfully"));
    }

    @PostMapping("/analyze/{id}")
    public ResponseEntity<Map<String, Object>> analyze(@PathVariable Integer id) {   // ⚠ Integer
        manualAnalysisService.startAnalysis(id);
        return ResponseEntity.accepted().body(Map.of("manualId", id, "status", "ANALYZING"));
    }

    @GetMapping("/status/{id}")
    public Map<String, Object> status(@PathVariable Integer id) {                    // ⚠ Integer
        return Map.of("manualId", id, "status", manualAnalysisService.getStatus(id));
    }
}