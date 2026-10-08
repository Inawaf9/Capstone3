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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final jakarta.persistence.EntityManager entityManager;

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final KilometerRecordRepository kilometerRecordRepository;
    private final VinClient vinClient;
    private final KilometerRecordService kilometerRecordService;
    private final com.nawaf.capstone3.Repository.NotificationRepository notificationRepository;
    private final jakarta.validation.Validator validator;
    private final org.springframework.transaction.support.TransactionTemplate transactions;

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

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void addVehicle(Integer userId, Vehicle vehicle) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle existingVehicle = vehicle.getVin() == null ? null : vehicleRepository.findVehicleByVin(vehicle.getVin());

        if (existingVehicle != null) throw new ApiException("VIN already exists");

        vehicle.setId(null);
        vehicle.setKilometerRecords(new java.util.LinkedHashSet<>());
        vehicle.setMaintenanceRecords(new java.util.LinkedHashSet<>());
        vehicle.setMaintenanceRules(new java.util.LinkedHashSet<>());
        vehicle.setAiChatHistories(new java.util.LinkedHashSet<>());
        vehicle.setCreatedAt(null);
        vehicle.setUser(user);
        vehicleRepository.save(vehicle);
        if (vehicle.getCurrentKilometers() != null)
            kilometerRecordService.recordCurrentReading(vehicle, vehicle.getCurrentKilometers(), "Initial odometer");
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void updateVehicle(Integer userId, Integer vehicleId, Vehicle updateVehicle) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = findFreshVehicleForUpdate(vehicleId);

        if (vehicle == null || !Objects.equals(vehicle.getUser().getId(), userId)) throw new ApiException("Vehicle not found or does not belong to this user");

        Vehicle vehicleVin = updateVehicle.getVin() == null ? null : vehicleRepository.findVehicleByVin(updateVehicle.getVin());

        if (vehicleVin != null && !vehicleVin.getId().equals(vehicleId)) throw new ApiException("VIN already exists");

        if (!Objects.equals(vehicle.getVin(), updateVehicle.getVin()) && !vehicle.getMaintenanceRules().isEmpty())
            throw new ApiException("VIN cannot change while vehicle-specific maintenance rules exist");
        vehicle.setVin(updateVehicle.getVin());
        vehicle.setMake(updateVehicle.getMake());
        vehicle.setModel(updateVehicle.getModel());
        vehicle.setYear(updateVehicle.getYear());
        vehicle.setEngine(updateVehicle.getEngine());
        vehicle.setFuelType(updateVehicle.getFuelType());
        if (updateVehicle.getCurrentKilometers() != null
                && !Objects.equals(vehicle.getCurrentKilometers(), updateVehicle.getCurrentKilometers())) {
            kilometerRecordService.recordCurrentReading(vehicle, updateVehicle.getCurrentKilometers(), "Vehicle update");
        }

        vehicleRepository.save(vehicle);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void deleteVehicle(Integer userId, Integer vehicleId) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = findFreshVehicleForUpdate(vehicleId);

        if (vehicle == null || !Objects.equals(vehicle.getUser().getId(), userId)) throw new ApiException("Vehicle not found or does not belong to this user");

        if (!vehicle.getMaintenanceRecords().isEmpty() || !vehicle.getMaintenanceRules().isEmpty()
                || !vehicle.getKilometerRecords().isEmpty() || !vehicle.getAiChatHistories().isEmpty()
                || notificationRepository.existsByUserIdAndMessageStartingWith(userId, "[maintenance:" + vehicleId + ":")
                || notificationRepository.existsByUserIdAndMessageStartingWith(userId, "[report:" + vehicleId + ":"))
            throw new ApiException("Vehicle has history; deletion would lose associated data");
        vehicleRepository.delete(vehicle);
    }

    public void decodeVin(Integer userId, String vin) {
        if (vin == null || !vin.matches("^[A-HJ-NPR-Z0-9]{17}$")) throw new ApiException("Invalid VIN");
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle existingVehicle = vehicleRepository.findVehicleByVin(vin);

        if (existingVehicle != null) throw new ApiException("VIN already exists");

        VinResponse response = vinClient.decodeVin(vin);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) throw new ApiException("Unable to decode VIN");

        VinResult result = response.getResults().getFirst();

        if (result == null || !"0".equals(result.getErrorCode()) || !vin.equalsIgnoreCase(result.getVin()))
            throw new ApiException("VIN provider could not validate this vehicle");
        if (result.getModel() == null || result.getModel().isBlank()) throw new ApiException("Vehicle model not found");
        if (result.getMake() == null || result.getMake().isEmpty()) throw new ApiException("Invalid VIN or vehicle data not found");
        if (result.getModelYear() == null || result.getModelYear().isEmpty()) throw new ApiException("Vehicle year not found");

        Vehicle vehicle = new Vehicle();

        vehicle.setVin(vin);
        vehicle.setMake(result.getMake());
        vehicle.setModel(result.getModel());
        try {
            vehicle.setYear(Integer.parseInt(result.getModelYear()));
        } catch (NumberFormatException exception) {
            throw new ApiException("VIN provider returned an invalid year");
        }
        vehicle.setFuelType(result.getFuelType());
        // VIN decoding does not provide an odometer reading.
        vehicle.setCurrentKilometers(null);
        vehicle.setUser(user);

        if (result.getDisplacementL() != null && result.getEngineCylinders() != null) {
            vehicle.setEngine(result.getDisplacementL() + "L " + result.getEngineCylinders() + " Cylinders");
        }

        if (!validator.validate(vehicle).isEmpty()) throw new ApiException("VIN provider returned invalid vehicle fields");
        transactions.executeWithoutResult(status -> {
            if (userRepository.findUserById(userId) == null) throw new ApiException("User no longer exists");
            vehicleRepository.save(vehicle);
        });
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

    // A locking query can return an already managed instance; refresh it after acquiring the lock.
    private Vehicle findFreshVehicleForUpdate(Integer id) {
        entityManager.flush();
        Vehicle vehicle = vehicleRepository.findVehicleForUpdate(id);
        if (vehicle != null) entityManager.refresh(vehicle, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return vehicle;
    }
}
