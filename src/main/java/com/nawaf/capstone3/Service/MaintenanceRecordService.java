package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.DTO.CompleteMaintenanceDTO;
import com.nawaf.capstone3.DTO.DueMaintenanceDTO;
import com.nawaf.capstone3.DTO.UpdateNoteDTO;
import com.nawaf.capstone3.DTO.VehicleMaintenanceSummaryDTO;
import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;
import com.nawaf.capstone3.Repository.KilometerRecordRepository;

@Service
@RequiredArgsConstructor
public class MaintenanceRecordService {
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRuleRepository maintenanceRuleRepository;
    private final KilometerRecordRepository kilometerRecordRepository;
    private final KilometerRecordService kilometerRecordService;
    private final jakarta.persistence.EntityManager entityManager;

    public List<DueMaintenanceDTO> getDueMaintenances(Integer vehicleId) {
        return scheduledMaintenance(vehicleId, "DUE");
    }

    public List<DueMaintenanceDTO> getOverdueMaintenances(Integer vehicleId) {
        return scheduledMaintenance(vehicleId, "OVERDUE");
    }

    public List<DueMaintenanceDTO> getUpcomingMaintenance(Integer vehicleId) {
        return scheduledMaintenance(vehicleId, "UPCOMING");
    }

    private List<DueMaintenanceDTO> scheduledMaintenance(Integer vehicleId, String requestedStatus) {
        Vehicle vehicle = requireVehicle(vehicleId);
        if (vehicle.getCurrentKilometers() == null)
            throw new ApiException("A current odometer reading is required");
        var completedRuleIds = maintenanceRecordRepository.findMaintenanceRecordByVehicle(vehicle).stream()
                .map(record -> record.getMaintenanceRule().getId()).collect(java.util.stream.Collectors.toSet());
        // These endpoints implement the requested 1,000-km reporting window. Notification thresholds are separate.
        // Absolute scheduled mileage is never added to the last completed service mileage.
        return maintenanceRuleRepository.findMaintenanceRuleByVehicle(vehicle).stream()
                .filter(rule -> rule.getKilometers() != null && !completedRuleIds.contains(rule.getId()))
                .map(rule -> {
                    long remaining = (long) rule.getKilometers() - vehicle.getCurrentKilometers();
                    String state = remaining < -1000 ? "OVERDUE" : remaining > 1000 ? "UPCOMING" : "DUE";
                    return new DueMaintenanceDTO(rule.getId(), rule.getServiceName(), rule.getKilometers(),
                            vehicle.getCurrentKilometers(), remaining, state);
                })
                .filter(item -> requestedStatus.equals(item.status()))
                .sorted(java.util.Comparator.comparing(DueMaintenanceDTO::dueKilometers)
                        .thenComparing(DueMaintenanceDTO::maintenanceRuleId))
                .toList();
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public MaintenanceRecord completeMaintenance(Integer vehicleId, Integer maintenanceRuleId, CompleteMaintenanceDTO input) {
        Vehicle vehicle = findFreshVehicleForUpdate(vehicleId);
        if (vehicle == null) throw new ApiException("Vehicle not found");
        MaintenanceRule rule = findFreshRuleForUpdate(maintenanceRuleId);
        if (rule == null) throw new ApiException("Maintenance rule not found");
        if (!Objects.equals(rule.getVehicle().getId(), vehicleId))
            throw new ApiException("Maintenance rule does not belong to this vehicle");
        if (rule.getKilometers() != null && maintenanceRecordRepository.existsByMaintenanceRuleId(maintenanceRuleId))
            throw new ApiException("This scheduled maintenance rule is already completed");
        validateCost(input.cost());
        if (input.kilometers() == null || input.kilometers() < 0)
            throw new ApiException("Completed service mileage must be nonnegative");
        if (vehicle.getCurrentKilometers() == null || input.kilometers() > vehicle.getCurrentKilometers())
            kilometerRecordService.recordCurrentReading(vehicle, input.kilometers(), "Completed maintenance");
        MaintenanceRecord record = new MaintenanceRecord();
        record.setKilometers(input.kilometers());
        record.setServiceDate(LocalDate.now());
        record.setCost(input.cost());
        record.setWorkshop(input.workshop());
        record.setNote(input.note());
        // Reuse association and chronology validation; both writes share this transaction.
        addMaintenanceRecord(vehicleId, maintenanceRuleId, record);
        return record;
    }

    public VehicleMaintenanceSummaryDTO getVehicleMaintenanceHistory(Integer vehicleId) {
        Vehicle vehicle = requireVehicle(vehicleId);
        var records = maintenanceRecordRepository.findByVehicleIdOrderByServiceDateDescIdDesc(vehicleId);
        return new VehicleMaintenanceSummaryDTO(vehicle.getId(), vehicle.getMake(), vehicle.getModel(),
                vehicle.getCurrentKilometers(), records.size(), records);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public MaintenanceRecord updateNoteByRecordId(Integer id, UpdateNoteDTO input) {
        if (input.note() == null || input.note().length() > 1000)
            throw new ApiException("Note is required and must not exceed 1000 characters");
        MaintenanceRecord record = lockedRecord(id);
        record.setNote(input.note());
        return maintenanceRecordRepository.save(record);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public MaintenanceRecord updateCostByRecordId(Integer id, Double cost) {
        validateCost(cost);
        MaintenanceRecord record = lockedRecord(id);
        record.setCost(cost);
        return maintenanceRecordRepository.save(record);
    }

    public List<MaintenanceRecord> getRecordsByServiceName(String serviceName) {
        if (serviceName == null || serviceName.isBlank() || serviceName.length() > 100)
            throw new ApiException("Service name must contain 1–100 characters");
        return maintenanceRecordRepository.findByMaintenanceRule_ServiceNameIgnoreCaseOrderByServiceDateDescIdDesc(serviceName.strip());
    }

    public List<MaintenanceRecord> getRecordsByVehicleIdAndWorkshop(Integer vehicleId, String workshop) {
        requireVehicle(vehicleId);
        if (workshop == null || workshop.isBlank() || workshop.length() > 150)
            throw new ApiException("Workshop must contain 1–150 characters");
        return maintenanceRecordRepository.findByVehicleIdAndWorkshopIgnoreCaseOrderByServiceDateDescIdDesc(vehicleId, workshop.strip());
    }

    private Vehicle requireVehicle(Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        if (vehicle == null) throw new ApiException("Vehicle not found");
        return vehicle;
    }

    private MaintenanceRecord lockedRecord(Integer id) {
        MaintenanceRecord record = getMaintenanceRecordById(id);
        findFreshVehicleForUpdate(record.getVehicle().getId());
        entityManager.refresh(record, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return record;
    }

    private void validateCost(Double cost) {
        if (cost == null || !Double.isFinite(cost) || cost < 0)
            throw new ApiException("Cost must be a finite nonnegative amount");
    }

    public List<MaintenanceRecord> getMaintenanceRecords() {
        return maintenanceRecordRepository.findAll();
    }

    public MaintenanceRecord getMaintenanceRecordById(Integer id) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(id);
        if (maintenanceRecord == null) throw new ApiException("Maintenance record not found");

        return maintenanceRecord;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void addMaintenanceRecord(Integer vehicleId, Integer maintenanceRuleId, MaintenanceRecord maintenanceRecord) {
        Vehicle vehicle = findFreshVehicleForUpdate(vehicleId);
        if (vehicle == null) throw new ApiException("Vehicle not found");

        MaintenanceRule maintenanceRule = findFreshRuleForUpdate(maintenanceRuleId);
        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        if (!maintenanceRule.getVehicle().getId().equals(vehicleId)) throw new ApiException("Maintenance rule does not belong to this vehicle");

        validateCompletion(vehicle, maintenanceRecord, null);
        maintenanceRecord.setId(null);
        maintenanceRecord.setReceipts(new java.util.LinkedHashSet<>());
        maintenanceRecord.setNotifications(new java.util.LinkedHashSet<>());
        maintenanceRecord.setVehicle(vehicle);
        maintenanceRecord.setMaintenanceRule(maintenanceRule);

        maintenanceRecordRepository.save(maintenanceRecord);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void updateMaintenanceRecord(Integer id, MaintenanceRecord updateMaintenanceRecord) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(id);
        if (maintenanceRecord == null) throw new ApiException("Maintenance record not found");

        Vehicle vehicle = findFreshVehicleForUpdate(maintenanceRecord.getVehicle().getId());
        validateCompletion(vehicle, updateMaintenanceRecord, id);
        maintenanceRecord.setKilometers(updateMaintenanceRecord.getKilometers());
        maintenanceRecord.setServiceDate(updateMaintenanceRecord.getServiceDate());
        maintenanceRecord.setCost(updateMaintenanceRecord.getCost());
        maintenanceRecord.setWorkshop(updateMaintenanceRecord.getWorkshop());
        maintenanceRecord.setNote(updateMaintenanceRecord.getNote());

        maintenanceRecordRepository.save(maintenanceRecord);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void deleteMaintenanceRecord(Integer id) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(id);
        if (maintenanceRecord == null) throw new ApiException("Maintenance record not found");

        findFreshVehicleForUpdate(maintenanceRecord.getVehicle().getId());
        if (!maintenanceRecord.getReceipts().isEmpty() || !maintenanceRecord.getNotifications().isEmpty())
            throw new ApiException("Maintenance record has receipts or notifications; preserve its history");
        maintenanceRecordRepository.delete(maintenanceRecord);
    }

    public Double getMaintenanceCostByYear(Integer vehicleId ,Integer year){
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        if (vehicle == null) {
            throw new ApiException("Vehicle not found");
        }
        if (year == null || year < 1886 || year > LocalDate.now().getYear()) throw new ApiException("Invalid report year");
        return maintenanceRecordRepository.getTotalCostByVehicleAndYear(vehicleId,year);
    }

    private void validateCompletion(Vehicle vehicle, MaintenanceRecord input, Integer excludedId) {
        if (input.getServiceDate() == null || input.getServiceDate().isAfter(LocalDate.now()))
            throw new ApiException("Completed service date must be today or earlier");
        if (input.getKilometers() == null || input.getKilometers() < 0)
            throw new ApiException("Completed service mileage must be nonnegative");
        if (input.getCost() == null || !Double.isFinite(input.getCost()) || input.getCost() < 0)
            throw new ApiException("Cost must be a finite nonnegative amount");
        if (vehicle.getCurrentKilometers() == null || input.getKilometers() > vehicle.getCurrentKilometers())
            throw new ApiException("Record a current odometer reading at least as high as the service mileage first");
        for (MaintenanceRecord record : maintenanceRecordRepository.findMaintenanceRecordByVehicle(vehicle)) {
            if (!Objects.equals(record.getId(), excludedId))
                checkChronology(input, record.getServiceDate(), record.getKilometers());
        }
        for (var reading : kilometerRecordRepository.findKilometerRecordsByVehicle(vehicle))
            checkChronology(input, reading.getRecordedAt().toLocalDate(), reading.getKilometers());
    }

    private void checkChronology(MaintenanceRecord input, LocalDate date, int kilometers) {
        if ((date.isBefore(input.getServiceDate()) && kilometers > input.getKilometers())
                || (date.isAfter(input.getServiceDate()) && kilometers < input.getKilometers()))
            throw new ApiException("Service mileage conflicts with the vehicle's dated history");
    }

    // A locking query can return an already managed instance; refresh it after acquiring the lock.
    private Vehicle findFreshVehicleForUpdate(Integer id) {
        entityManager.flush();
        Vehicle vehicle = vehicleRepository.findVehicleForUpdate(id);
        if (vehicle != null) entityManager.refresh(vehicle, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return vehicle;
    }

    private MaintenanceRule findFreshRuleForUpdate(Integer id) {
        entityManager.flush();
        MaintenanceRule rule = maintenanceRuleRepository.findRuleForUpdate(id);
        if (rule != null) entityManager.refresh(rule, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return rule;
    }
}
