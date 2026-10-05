package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceRecordService {
    private final MaintenanceRecordRepository maintenanceRecordRepository;


    public List<MaintenanceRecord> getAll(){
        return maintenanceRecordRepository.findAll();
    }

    public void addMaintenanceRecord(MaintenanceRecord maintenanceRecord){
        maintenanceRecordRepository.save(maintenanceRecord);
    }

    public void updateMaintenanceRecord(Integer maintenanceRecordId, MaintenanceRecord maintenanceRecord){
        MaintenanceRecord oldMaintenanceRecord=maintenanceRecordRepository.findMaintenanceRecordById(maintenanceRecordId);
        if(oldMaintenanceRecord==null){
            throw new ApiException("maintenance record id not found ");
        }
        oldMaintenanceRecord.setCost(maintenanceRecord.getCost());
        oldMaintenanceRecord.setKilometers(maintenanceRecord.getKilometers());
        oldMaintenanceRecord.setMaintenanceRule(maintenanceRecord.getMaintenanceRule());
        oldMaintenanceRecord.setNote(maintenanceRecord.getNote());
        oldMaintenanceRecord.setReceipts(maintenanceRecord.getReceipts());
        oldMaintenanceRecord.setServiceDate(maintenanceRecord.getServiceDate());
        oldMaintenanceRecord.setVehicle(maintenanceRecord.getVehicle());
        oldMaintenanceRecord.setWorkshop(maintenanceRecord.getWorkshop());

        maintenanceRecordRepository.save(oldMaintenanceRecord);
    }

    public void deleteMaintenanceRecord(Integer maintenanceId){
        MaintenanceRecord maintenanceRecord=maintenanceRecordRepository.findMaintenanceRecordById(maintenanceId);
        if(maintenanceRecord==null){
            throw new ApiException("maintenance record id not found");

        }
        maintenanceRecordRepository.deleteById(maintenanceId);
    }
}