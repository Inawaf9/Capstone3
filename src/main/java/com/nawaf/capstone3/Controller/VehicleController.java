package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Service.VehicleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicle")
@AllArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getVehicles() {
        return ResponseEntity.status(200).body(vehicleService.getVehicles());
    }

    @GetMapping("/get/{userId}/{vehicleId}")
    public ResponseEntity<?> getVehicleById(@PathVariable Integer userId, @PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(vehicleService.getVehicleById(userId, vehicleId));
    }

    @GetMapping("/get/user/{userId}")
    public ResponseEntity<?> getVehiclesByUser(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(vehicleService.getVehiclesByUser(userId));
    }

    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addVehicle(@PathVariable Integer userId, @Valid @RequestBody Vehicle vehicle) {
        vehicleService.addVehicle(userId, vehicle);
        return ResponseEntity.status(201).body(new ApiResponse("Vehicle added successfully"));
    }

    @PutMapping("/update/{userId}/{vehicleId}")
    public ResponseEntity<?> updateVehicle(@PathVariable Integer userId, @PathVariable Integer vehicleId, @Valid @RequestBody Vehicle vehicle) {
        vehicleService.updateVehicle(userId, vehicleId, vehicle);
        return ResponseEntity.status(200).body(new ApiResponse("Vehicle updated successfully"));
    }

    @DeleteMapping("/delete/{userId}/{vehicleId}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Integer userId, @PathVariable Integer vehicleId) {
        vehicleService.deleteVehicle(userId, vehicleId);
        return ResponseEntity.status(200).body(new ApiResponse("Vehicle deleted successfully"));
    }

    @PostMapping("/decode-vin/{userId}")
    public ResponseEntity<?> decodeVin(@PathVariable Integer userId, @RequestParam String vin) {
        vehicleService.decodeVin(userId, vin);
        return ResponseEntity.status(201).body(new ApiResponse("Vehicle added successfully"));
    }

    @GetMapping("/{userId}/{vehicleId}/summary")
    public ResponseEntity<?> getVehicleSummary(@PathVariable Integer userId, @PathVariable Integer vehicleId) {
        return ResponseEntity.status(200).body(vehicleService.getVehicleSummary(userId, vehicleId));
    }

    @GetMapping("/user/{userId}/vehicles-summary")
    public ResponseEntity<?> getUserVehiclesSummary(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(vehicleService.getUserVehiclesSummary(userId));
    }
}