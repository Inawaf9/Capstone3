package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.DTO.KilometerRecordDTO;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KilometerRecordService {
    private final jakarta.persistence.EntityManager entityManager;

    private final KilometerRecordRepository kilometerRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final com.nawaf.capstone3.Repository.MaintenanceRecordRepository maintenanceRecordRepository;

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

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void addKilometerRecord(Integer userId, Integer vehicleId, KilometerRecord input) {
        Vehicle vehicle = lockedVehicle(userId, vehicleId);
        recordCurrentReading(vehicle, input.getKilometers(), input.getNote());
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void recordKilometers(Integer userId, Integer vehicleId, KilometerRecordDTO dto) {
        recordCurrentReading(lockedVehicle(userId, vehicleId), dto.getKilometers(), dto.getNote());
    }

    /** Caller holds the vehicle lock (or is creating a new vehicle) within its transaction. */
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void recordCurrentReading(Vehicle vehicle, Integer kilometers, String note) {
        if (kilometers == null || kilometers < 0) throw new ApiException("Kilometers must be nonnegative");
        if (vehicle.getCurrentKilometers() != null && kilometers < vehicle.getCurrentKilometers())
            throw new ApiException("Kilometers cannot be less than current vehicle kilometers");
        var latest = kilometerRecordRepository.findTopByVehicleOrderByRecordedAtDescIdDesc(vehicle);
        if (latest != null && kilometers < latest.getKilometers())
            throw new ApiException("Kilometers cannot regress from the latest reading");
        KilometerRecord record = new KilometerRecord();
        record.setKilometers(kilometers);
        record.setNote(note);
        record.setVehicle(vehicle);
        kilometerRecordRepository.save(record);
        vehicle.setCurrentKilometers(kilometers);
        vehicleRepository.save(vehicle);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void updateKilometerRecord(Integer userId, Integer vehicleId, Integer id, KilometerRecord input) {
        Vehicle vehicle = lockedVehicle(userId, vehicleId);
        List<KilometerRecord> history = kilometerRecordRepository.findAllByVehicleOrderByRecordedAtDescIdDesc(vehicle);
        int index = -1;
        for (int i = 0; i < history.size(); i++) if (Objects.equals(history.get(i).getId(), id)) index = i;
        if (index < 0) throw new ApiException("Kilometer record not found for this vehicle");
        int km = input.getKilometers() == null ? -1 : input.getKilometers();
        if (km < 0 || (index > 0 && km > history.get(index - 1).getKilometers())
                || (index + 1 < history.size() && km < history.get(index + 1).getKilometers()))
            throw new ApiException("Historical reading must fit between neighboring readings");
        if (index == 0 && vehicle.getCurrentKilometers() != null && km < vehicle.getCurrentKilometers())
            throw new ApiException("The latest reading cannot reduce the current odometer");
        KilometerRecord record = history.get(index);
        for (var completed : maintenanceRecordRepository.findMaintenanceRecordByVehicle(vehicle)) {
            var readingDate = record.getRecordedAt().toLocalDate();
            if ((completed.getServiceDate().isBefore(readingDate) && completed.getKilometers() > km)
                    || (completed.getServiceDate().isAfter(readingDate) && completed.getKilometers() < km))
                throw new ApiException("Reading conflicts with completed maintenance history");
        }
        record.setKilometers(km);
        record.setNote(input.getNote());
        kilometerRecordRepository.save(record);
        if (index == 0) {
            vehicle.setCurrentKilometers(km);
            vehicleRepository.save(vehicle);
        }
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void deleteKilometerRecord(Integer userId, Integer vehicleId, Integer id) {
        Vehicle vehicle = lockedVehicle(userId, vehicleId);
        var record = kilometerRecordRepository.findKilometerRecordByIdAndVehicle(id, vehicle);
        if (record == null) throw new ApiException("Kilometer record not found for this vehicle");
        var latest = kilometerRecordRepository.findTopByVehicleOrderByRecordedAtDescIdDesc(vehicle);
        if (latest != null && Objects.equals(latest.getId(), id))
            throw new ApiException("Keep the latest odometer reading; only historical readings can be deleted");
        kilometerRecordRepository.delete(record);
    }

    private Vehicle lockedVehicle(Integer userId, Integer vehicleId) {
        entityManager.flush();
        Vehicle vehicle = vehicleRepository.findVehicleForUpdate(vehicleId);
        if (vehicle != null) entityManager.refresh(vehicle, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (vehicle == null || !Objects.equals(vehicle.getUser().getId(), userId))
            throw new ApiException("Vehicle not found or does not belong to this user");
        return vehicle;
    }

    public KilometerRecord getLatestKilometerRecord(Integer userId, Integer vehicleId) {
        Vehicle vehicle = getUserVehicle(userId, vehicleId);

        KilometerRecord kilometerRecord = kilometerRecordRepository.findTopByVehicleOrderByRecordedAtDescIdDesc(vehicle);

        if (kilometerRecord == null) throw new ApiException("No kilometer records found");

        return kilometerRecord;
    }

    public List<KilometerRecord> getKilometerHistory(Integer userId, Integer vehicleId) {
        Vehicle vehicle = getUserVehicle(userId, vehicleId);

        return kilometerRecordRepository.findAllByVehicleOrderByRecordedAtDescIdDesc(vehicle);
    }

    public Double getAverageMonthlyKilometers(Integer userId, Integer vehicleId) {
        Vehicle vehicle = getUserVehicle(userId, vehicleId);
        List<KilometerRecord> records = kilometerRecordRepository.findAllByVehicleOrderByRecordedAtDescIdDesc(vehicle);

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
        List<KilometerRecord> records = kilometerRecordRepository.findAllByVehicleOrderByRecordedAtDescIdDesc(vehicle);

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

        List<KilometerRecord> records = kilometerRecordRepository.findAllByVehicleOrderByRecordedAtDescIdDesc(vehicle)
                .stream().filter(r -> r.getRecordedAt().isBefore(endOfMonth)).toList();
        if (records.isEmpty()) throw new ApiException("No odometer readings available");
        KilometerRecord end = records.getFirst();
        KilometerRecord start = records.stream().filter(r -> r.getRecordedAt().isBefore(startOfMonth))
                .findFirst().orElse(records.getLast());
        return Math.max(0, end.getKilometers() - start.getKilometers());
    }

    private Vehicle getUserVehicle(Integer userId, Integer vehicleId) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);

        if (vehicle == null) throw new ApiException("Vehicle not found or does not belong to this user");

        return vehicle;
    }
}
