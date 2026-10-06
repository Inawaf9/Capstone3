package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.UserManual;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.UserManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceRuleService {

    private final MaintenanceRuleRepository maintenanceRuleRepository;
    private final UserManualRepository userManualRepository;



    public List<MaintenanceRule>getAll(){
        return maintenanceRuleRepository.findAll();
    }
//اضفت التحقق من المستخدم
    public void addMaintenanceRule(MaintenanceRule maintenanceRule){
        UserManual userManual=userManualRepository.findUserManualById(maintenanceRule.getUserManual().getId());
        if(userManual==null){
            throw new ApiException("user manual id not found ");
        }
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
        oldMaintenanceRule.setMonthInterval(maintenanceRule.getMonthInterval());
        oldMaintenanceRule.setServiceName(maintenanceRule.getServiceName());
        oldMaintenanceRule.setTriggerType(maintenanceRule.getTriggerType());
        oldMaintenanceRule.setNotes(maintenanceRule.getNotes());


        //وضعتها كتعليق لمناقشة اذا نخليها بالتعديل او لا
//        oldMaintenanceRule.setMaintenanceRecords(maintenanceRule.getMaintenanceRecords());
//        oldMaintenanceRule.setUserManual(maintenanceRule.getUserManual());


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