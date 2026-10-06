package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.DTO.KilometerRecordDTO;
import com.nawaf.capstone3.Model.KilometerRecord;
import com.nawaf.capstone3.Model.User;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.KilometerRecordRepository;
import com.nawaf.capstone3.Repository.UserRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class KilometerRecordService {

    private final KilometerRecordRepository kilometerRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public List<KilometerRecord> getKilometerRecords() {
        return kilometerRecordRepository.findAll();
    }

    public KilometerRecord getKilometerRecordById(Integer userId, Integer vehicleId, Integer id) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        KilometerRecord kilometerRecord = kilometerRecordRepository.findKilometerRecordByIdAndVehicle(id, vehicle);

        if (kilometerRecord == null) throw new ApiException("Kilometer record not found or does not belong to this vehicle");

        return kilometerRecord;
    }

    public void addKilometerRecord(Integer userId, Integer vehicleId, KilometerRecord kilometerRecord) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        kilometerRecord.setVehicle(vehicle);

        kilometerRecordRepository.save(kilometerRecord);
    }

    public void updateKilometerRecord(Integer userId, Integer vehicleId, Integer id, KilometerRecord updateKilometerRecord) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        KilometerRecord kilometerRecord = kilometerRecordRepository.findKilometerRecordByIdAndVehicle(id, vehicle);

        if (kilometerRecord == null) throw new ApiException("Kilometer record not found or does not belong to this vehicle");

        kilometerRecord.setKilometers(updateKilometerRecord.getKilometers());
        kilometerRecord.setNote(updateKilometerRecord.getNote());

        kilometerRecordRepository.save(kilometerRecord);
    }

    public void deleteKilometerRecord(Integer userId, Integer vehicleId, Integer id) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        KilometerRecord kilometerRecord = kilometerRecordRepository.findKilometerRecordByIdAndVehicle(id, vehicle);

        if (kilometerRecord == null) throw new ApiException("Kilometer record not found or does not belong to this vehicle");

        kilometerRecordRepository.delete(kilometerRecord);
    }

    public void recordKilometers(Integer userId, Integer vehicleId, KilometerRecordDTO dto) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");
        if (vehicle.getCurrentKilometers() != null && dto.getKilometers() < vehicle.getCurrentKilometers()) throw new ApiException("Kilometers cannot be less than current vehicle kilometers");

        KilometerRecord kilometerRecord = new KilometerRecord();

        kilometerRecord.setKilometers(dto.getKilometers());
        kilometerRecord.setNote(dto.getNote());
        kilometerRecord.setVehicle(vehicle);

        kilometerRecordRepository.save(kilometerRecord);

        vehicle.setCurrentKilometers(dto.getKilometers());
        vehicleRepository.save(vehicle);
    }

    public KilometerRecord getLatestKilometerRecord(Integer userId, Integer vehicleId) {
        Vehicle vehicle = getUserVehicle(userId, vehicleId);

        KilometerRecord kilometerRecord = kilometerRecordRepository.findTopByVehicleOrderByRecordedAtDesc(vehicle);

        if (kilometerRecord == null) throw new ApiException("No kilometer records found");

        return kilometerRecord;
    }

    public List<KilometerRecord> getKilometerHistory(Integer userId, Integer vehicleId) {
        Vehicle vehicle = getUserVehicle(userId, vehicleId);

        return kilometerRecordRepository.findAllByVehicleOrderByRecordedAtDesc(vehicle);
    }

    public Double getAverageMonthlyKilometers(Integer userId, Integer vehicleId) {
        Vehicle vehicle = getUserVehicle(userId, vehicleId);
        List<KilometerRecord> records = kilometerRecordRepository.findAllByVehicleOrderByRecordedAtDesc(vehicle);

        if (records.size() < 2) throw new ApiException("Not enough kilometer records");

        KilometerRecord latest = records.getFirst();
        KilometerRecord oldest = records.getLast();

        int distance = latest.getKilometers() - oldest.getKilometers();

        long days = ChronoUnit.DAYS.between(
                oldest.getRecordedAt().toLocalDate(),
                latest.getRecordedAt().toLocalDate()
        );

        if (days == 0) throw new ApiException("Not enough time between kilometer records");

        double months = days / 30.44;

        return distance / months;
    }

    public Integer getDistanceTraveled(Integer userId, Integer vehicleId) {
        Vehicle vehicle = getUserVehicle(userId, vehicleId);
        List<KilometerRecord> records = kilometerRecordRepository.findAllByVehicleOrderByRecordedAtDesc(vehicle);

        if (records.size() < 2) throw new ApiException("Not enough kilometer records");

        KilometerRecord latest = records.getFirst();
        KilometerRecord oldest = records.getLast();

        return latest.getKilometers() - oldest.getKilometers();
    }

    public Integer getMonthlyDistance(Integer userId, Integer vehicleId) {
        Vehicle vehicle = getUserVehicle(userId, vehicleId);

        LocalDateTime startOfMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        LocalDateTime endOfMonth = LocalDate.now()
                .plusMonths(1)
                .withDayOfMonth(1)
                .atStartOfDay();

        List<KilometerRecord> records = kilometerRecordRepository.findAllByVehicleAndRecordedAtBetweenOrderByRecordedAtAsc(
                vehicle,
                startOfMonth,
                endOfMonth
        );

        if (records.size() < 2) throw new ApiException("Not enough kilometer records for this month");

        KilometerRecord first = records.getFirst();
        KilometerRecord last = records.getLast();

        return last.getKilometers() - first.getKilometers();
    }

    private Vehicle getUserVehicle(Integer userId, Integer vehicleId) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        return vehicle;
    }
}