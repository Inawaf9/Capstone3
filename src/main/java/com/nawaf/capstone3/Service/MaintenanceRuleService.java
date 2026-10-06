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

    public List<MaintenanceRule> getMaintenanceRules() {
        return maintenanceRuleRepository.findAll();
    }

    public MaintenanceRule getMaintenanceRuleById(Integer id) {
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(id);

        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        return maintenanceRule;
    }

    public void addMaintenanceRule(Integer userManualId, MaintenanceRule maintenanceRule) {
        UserManual userManual = userManualRepository.findUserManualById(userManualId);

        if (userManual == null) throw new ApiException("User manual not found");

        maintenanceRule.setUserManual(userManual);

        maintenanceRuleRepository.save(maintenanceRule);
    }

    public void updateMaintenanceRule(Integer id, MaintenanceRule updateMaintenanceRule) {
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(id);

        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        maintenanceRule.setServiceName(updateMaintenanceRule.getServiceName());
        maintenanceRule.setDescription(updateMaintenanceRule.getDescription());
        maintenanceRule.setTriggerType(updateMaintenanceRule.getTriggerType());
        maintenanceRule.setKilometerInterval(updateMaintenanceRule.getKilometerInterval());
        maintenanceRule.setMonthInterval(updateMaintenanceRule.getMonthInterval());
        maintenanceRule.setCondition(updateMaintenanceRule.getCondition());
        maintenanceRule.setNotes(updateMaintenanceRule.getNotes());

        maintenanceRuleRepository.save(maintenanceRule);
    }

    public void deleteMaintenanceRule(Integer id) {
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(id);

        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        maintenanceRuleRepository.delete(maintenanceRule);
    }
}