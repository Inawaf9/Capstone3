package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Client.VinClient;
import com.nawaf.capstone3.DTO.VehicleSummaryDTO;
import com.nawaf.capstone3.DTO.VinResponse;
import com.nawaf.capstone3.DTO.VinResult;
import com.nawaf.capstone3.Model.KilometerRecord;
import com.nawaf.capstone3.Model.User;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.KilometerRecordRepository;
import com.nawaf.capstone3.Repository.UserRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final KilometerRecordRepository kilometerRecordRepository;
    private final VinClient vinClient;

    public List<Vehicle> getVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicleById(Integer userId, Integer vehicleId) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        return vehicle;
    }

    public List<Vehicle> getVehiclesByUser(Integer userId) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        return vehicleRepository.findVehiclesByUser(user);
    }

    public void addVehicle(Integer userId, Vehicle vehicle) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle existingVehicle = vehicleRepository.findVehicleByVin(vehicle.getVin());

        if (existingVehicle != null) throw new ApiException("VIN already exists");

        vehicle.setUser(user);
        vehicleRepository.save(vehicle);
    }

    public void updateVehicle(Integer userId, Integer vehicleId, Vehicle updateVehicle) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        Vehicle vehicleVin = vehicleRepository.findVehicleByVin(updateVehicle.getVin());

        if (vehicleVin != null && !vehicleVin.getId().equals(vehicleId)) throw new ApiException("VIN already exists");

        vehicle.setVin(updateVehicle.getVin());
        vehicle.setMake(updateVehicle.getMake());
        vehicle.setModel(updateVehicle.getModel());
        vehicle.setYear(updateVehicle.getYear());
        vehicle.setEngine(updateVehicle.getEngine());
        vehicle.setFuelType(updateVehicle.getFuelType());
        vehicle.setCurrentKilometers(updateVehicle.getCurrentKilometers());

        vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Integer userId, Integer vehicleId) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        vehicleRepository.delete(vehicle);
    }

    public void decodeVin(Integer userId, String vin) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle existingVehicle = vehicleRepository.findVehicleByVin(vin);

        if (existingVehicle != null) throw new ApiException("VIN already exists");

        VinResponse response = vinClient.decodeVin(vin);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) throw new ApiException("Unable to decode VIN");

        VinResult result = response.getResults().getFirst();

        if (result.getMake() == null || result.getMake().isEmpty()) throw new ApiException("Invalid VIN or vehicle data not found");
        if (result.getModelYear() == null || result.getModelYear().isEmpty()) throw new ApiException("Vehicle year not found");

        Vehicle vehicle = new Vehicle();

        vehicle.setVin(vin);
        vehicle.setMake(result.getMake());
        vehicle.setModel(result.getModel());
        vehicle.setYear(Integer.parseInt(result.getModelYear()));
        vehicle.setFuelType(result.getFuelType());
        vehicle.setCurrentKilometers(0);
        vehicle.setUser(user);

        if (result.getDisplacementL() != null && result.getEngineCylinders() != null) {
            vehicle.setEngine(result.getDisplacementL() + "L " + result.getEngineCylinders() + " Cylinders");
        }

        vehicleRepository.save(vehicle);
    }

    public VehicleSummaryDTO getVehicleSummary(Integer userId, Integer vehicleId) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        return createVehicleSummary(vehicle);
    }

    public List<VehicleSummaryDTO> getUserVehiclesSummary(Integer userId) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        List<Vehicle> vehicles = vehicleRepository.findVehiclesByUser(user);
        List<VehicleSummaryDTO> summaries = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            summaries.add(createVehicleSummary(vehicle));
        }

        return summaries;
    }

    private VehicleSummaryDTO createVehicleSummary(Vehicle vehicle) {
        List<KilometerRecord> records = kilometerRecordRepository.findKilometerRecordsByVehicle(vehicle);
        Integer vehicleAge = LocalDate.now().getYear() - vehicle.getYear();

        return new VehicleSummaryDTO(
                vehicle.getId(),
                vehicle.getMake() + " " + vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getCurrentKilometers(),
                vehicleAge,
                records.size()
        );
    }
}