package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Service.VehicleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle")
@AllArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;


    // Get all vehicles
    @GetMapping("/get")
    public ResponseEntity<?> getAllVehicle() {
        return ResponseEntity.status(200).body(vehicleService.getAllVehicle());
    }


    // Get all vehicles for a user
    @GetMapping("/get/user/{userId}")
    public ResponseEntity<?> getAllVehicleForUser(@PathVariable Integer userId) {

        return ResponseEntity.status(200).body(vehicleService.getAllVehicleForUser(userId));
    }


    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addVehicle(@PathVariable Integer userId, @Valid @RequestBody Vehicle vehicle) {

        vehicleService.addVehicle(userId, vehicle);

        return ResponseEntity.status(200).body(new ApiResponse("Vehicle add successfully"));
    }


    @PutMapping("/update/{userId}/{vehicleId}")
    public ResponseEntity<?> updateVehicle(@PathVariable Integer userId, @PathVariable Integer vehicleId,@RequestBody @Valid Vehicle updateVehicle) {

        vehicleService.updateVehicle(userId, vehicleId, updateVehicle);
        return ResponseEntity.status(200).body(new ApiResponse("Vehicle update successfully"));
    }


    // Delete vehicle
    @DeleteMapping("/delete/{userId}/{vehicleId}")
    public ResponseEntity<?> delete(@PathVariable Integer userId, @PathVariable Integer vehicleId) {
        vehicleService.delete(userId, vehicleId);
        return ResponseEntity.status(200).body(new ApiResponse(" Vehicle delete successfully"));
    }
}