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


    public List<MaintenanceRecord> getAll(){
        return maintenanceRecordRepository.findAll();
    }
    //تم اضافة التعديل
    public void addMaintenanceRecord(MaintenanceRecord maintenanceRecord){
        Vehicle vehicle=vehicleRepository.findVehicleById(maintenanceRecord.getVehicle().getId());
        MaintenanceRule maintenanceRule=maintenanceRuleRepository.findMaintenanceRuleById(maintenanceRecord.getMaintenanceRule().getId());

        if(vehicle==null){
            throw new ApiException("vehicle id not  found");
        }
        if(maintenanceRule==null){
            throw new ApiException("maintenance rule id not found");
        }


        maintenanceRecordRepository.save(maintenanceRecord);
    }

    public void updateMaintenanceRecord(Integer maintenanceRecordId, MaintenanceRecord maintenanceRecord){
        MaintenanceRecord oldMaintenanceRecord=maintenanceRecordRepository.findMaintenanceRecordById(maintenanceRecordId);
        if(oldMaintenanceRecord==null){
            throw new ApiException("maintenance record id not found ");
        }
        oldMaintenanceRecord.setCost(maintenanceRecord.getCost());
        oldMaintenanceRecord.setKilometers(maintenanceRecord.getKilometers());
        oldMaintenanceRecord.setNote(maintenanceRecord.getNote());
        oldMaintenanceRecord.setServiceDate(maintenanceRecord.getServiceDate());
        oldMaintenanceRecord.setWorkshop(maintenanceRecord.getWorkshop());


        //ما نعدل عليها
//        oldMaintenanceRecord.setReceipts(maintenanceRecord.getReceipts());
//        oldMaintenanceRecord.setMaintenanceRule(maintenanceRecord.getMaintenanceRule());
//        oldMaintenanceRecord.setVehicle(maintenanceRecord.getVehicle());


        maintenanceRecordRepository.save(oldMaintenanceRecord);
    }

    public void deleteMaintenanceRecord(Integer maintenanceId){
        MaintenanceRecord maintenanceRecord=maintenanceRecordRepository.findMaintenanceRecordById(maintenanceId);
        if(maintenanceRecord==null){
            throw new ApiException("maintenance record id not found");

        }
        maintenanceRecordRepository.deleteById(maintenanceId);
    }

    //get by       id
    public MaintenanceRecord getMaintenanceRecordById(Integer maintenanceRecordId){
        MaintenanceRecord maintenanceRecord=maintenanceRecordRepository.findMaintenanceRecordById(maintenanceRecordId);
        if(maintenanceRecord==null){
            throw new ApiException("maintenance record ID not found");
        }
        return maintenanceRecord;
    }
}