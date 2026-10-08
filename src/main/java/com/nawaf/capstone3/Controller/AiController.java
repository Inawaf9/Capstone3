package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Service.AiService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/v1/ai")
@RestController
@AllArgsConstructor
public class AiController {
    private final AiService aiService;




    @PostMapping("/ask/{vehicleId}")
    public ResponseEntity<?> askAboutVehicle(@PathVariable Integer vehicleId, @RequestBody String question) {
        return ResponseEntity.status(200).body(new ApiResponse(aiService.askAboutVehicle(vehicleId, question)));
    }


    @PostMapping("/analyze-problem/{vehicleId}")
    public ResponseEntity<?> analyzeProblem(@PathVariable Integer vehicleId, @RequestBody String problem) {
        return ResponseEntity.status(200).body(new ApiResponse(aiService.analyzeProblem(vehicleId, problem)));
    }


    @PostMapping("/maintenance-advice/{vehicleId}")
    public ResponseEntity<?> maintenanceProblem(@PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(new ApiResponse(aiService.maintenanceProblem(vehicleId)));
    }


    @PostMapping("/summarize-history/{vehicleId}")
    public ResponseEntity<?> summarizeHistory(@PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(new ApiResponse(aiService.summarizeHistory(vehicleId)));
    }

}
