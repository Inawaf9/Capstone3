package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceRuleService {

    private final MaintenanceRuleRepository maintenanceRuleRepository;



    public List<MaintenanceRule>getAll(){
        return maintenanceRuleRepository.findAll();
    }

    public void addMaintenanceRule(MaintenanceRule maintenanceRule){
        maintenanceRuleRepository.save(maintenanceRule);
    }

    public void updateMaintenanceRule(Integer MaintenanceRuleId , MaintenanceRule maintenanceRule){
        MaintenanceRule oldMaintenanceRule= maintenanceRuleRepository.findMaintenanceRuleById(MaintenanceRuleId);
        if(oldMaintenanceRule==null){
            throw new ApiException("maintenance rule id not found ");
        }
        oldMaintenanceRule.setCondition(maintenanceRule.getCondition());
        oldMaintenanceRule.setDescription(maintenanceRule.getDescription());
        oldMaintenanceRule.setKilometerInterval(maintenanceRule.getKilometerInterval());
        oldMaintenanceRule.setMaintenanceRecords(maintenanceRule.getMaintenanceRecords());
        oldMaintenanceRule.setMonthInterval(maintenanceRule.getMonthInterval());
        oldMaintenanceRule.setServiceName(maintenanceRule.getServiceName());
        oldMaintenanceRule.setTriggerType(maintenanceRule.getTriggerType());
        oldMaintenanceRule.setUserManual(maintenanceRule.getUserManual());
        oldMaintenanceRule.setNotes(maintenanceRule.getNotes());

        maintenanceRuleRepository.save(oldMaintenanceRule);

    }

    public void deleteMaintenanceRule(Integer maintenanceRuleId){
        MaintenanceRule oldMaintenanceRule=maintenanceRuleRepository.findMaintenanceRuleById(maintenanceRuleId);
        if(oldMaintenanceRule==null){
            throw new ApiException("maintenance rule id not found can not be deleted");
        }

        maintenanceRuleRepository.deleteById(maintenanceRuleId);
    }


}