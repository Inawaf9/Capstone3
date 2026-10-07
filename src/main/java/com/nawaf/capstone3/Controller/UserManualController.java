package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.UserManual;
import com.nawaf.capstone3.Service.ManualAnalysisService;
import com.nawaf.capstone3.Service.UserManualService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/userManual")
public class UserManualController {
    private final UserManualService userManualService;
    private final ManualAnalysisService manualAnalysisService;


    @GetMapping("/get-all")
    public ResponseEntity<?>getAll(){
        List<UserManual> userManualList=userManualService.getAll();
        return ResponseEntity.status(200).body(userManualList);
    }
    @GetMapping("/get/{userManualId}")
    public ResponseEntity<?>getUserManualById(@PathVariable Integer userManualId) {
        UserManual userManual= userManualService.getUserManualById(userManualId);
        return ResponseEntity.status(200).body(userManual);
    }


    @PostMapping("/add/{vehicleId}")
    public ResponseEntity<?> addUserManual(
            @PathVariable Integer vehicleId,
            @Valid @RequestBody UserManual userManual) {

        userManualService.addUserManual(vehicleId, userManual);

        return ResponseEntity.status(200)
                .body(new ApiResponse("add successfully"));
    }

    @PutMapping  ("/update/{userManualId}")
    public ResponseEntity<?>updateUserManual(@PathVariable Integer userManualId ,@RequestBody UserManual userManual){
        userManualService.updateUserManual(userManualId,userManual);
        return ResponseEntity.status(200).body(new ApiResponse("update successfully"));
    }
    @DeleteMapping("/delete/{userManualId}")
    public ResponseEntity<?>deleteUserManual(@PathVariable Integer userManualId){
        userManualService.deleteUserManual(userManualId);
        return ResponseEntity.status(200).body(new ApiResponse("deleted successfully"));
    }




    // Ai


    @PostMapping("/analyze/{id}")
    public ResponseEntity<Map<String, Object>> analyze(@PathVariable Integer id) {   // ⚠ Integer
        manualAnalysisService.startAnalysis(id);
        return ResponseEntity.accepted().body(Map.of("manualId", id, "status", "ANALYZING"));
    }

    @GetMapping("/status/{id}")
    public Map<String, Object> status(@PathVariable Integer id) {                    // ⚠ Integer
        return Map.of("manualId", id, "status", manualAnalysisService.getStatus(id));
    }

    //4
    @GetMapping("/get-rules/{userManualId}")
    public ResponseEntity<?> getRules(@PathVariable Integer userManualId) {
        return ResponseEntity.status(200).body(userManualService.getRulesByUserManualId(userManualId));
    }


}