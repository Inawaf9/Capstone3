package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceRecordService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRuleRepository maintenanceRuleRepository;

    public List<MaintenanceRecord> getMaintenanceRecords() {
        return maintenanceRecordRepository.findAll();
    }

    public MaintenanceRecord getMaintenanceRecordById(Integer id) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(id);

        if (maintenanceRecord == null) throw new ApiException("Maintenance record not found");

        return maintenanceRecord;
    }

    public void addMaintenanceRecord(Integer vehicleId, Integer maintenanceRuleId, MaintenanceRecord maintenanceRecord) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(maintenanceRuleId);

        if (vehicle == null) throw new ApiException("Vehicle not found");
        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");
        if (!maintenanceRule.getUserManual().getVehicle().getId().equals(vehicleId)) throw new ApiException("Maintenance rule does not belong to this vehicle");

        maintenanceRecord.setVehicle(vehicle);
        maintenanceRecord.setMaintenanceRule(maintenanceRule);

        maintenanceRecordRepository.save(maintenanceRecord);
    }

    public void updateMaintenanceRecord(Integer id, MaintenanceRecord updateMaintenanceRecord) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(id);

        if (maintenanceRecord == null) throw new ApiException("Maintenance record not found");

        maintenanceRecord.setKilometers(updateMaintenanceRecord.getKilometers());
        maintenanceRecord.setServiceDate(updateMaintenanceRecord.getServiceDate());
        maintenanceRecord.setCost(updateMaintenanceRecord.getCost());
        maintenanceRecord.setWorkshop(updateMaintenanceRecord.getWorkshop());
        maintenanceRecord.setNote(updateMaintenanceRecord.getNote());

        maintenanceRecordRepository.save(maintenanceRecord);
    }

    public void deleteMaintenanceRecord(Integer id) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findMaintenanceRecordById(id);

        if (maintenanceRecord == null) throw new ApiException("Maintenance record not found");

        maintenanceRecordRepository.delete(maintenanceRecord);
    }
}