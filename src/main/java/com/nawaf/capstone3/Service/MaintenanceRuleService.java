package com.nawaf.capstone3.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Client.OpenRouterClient;
import com.nawaf.capstone3.Client.VehicleDatabaseClient;
import com.nawaf.capstone3.DTO.AI.MaintenanceAiRequest;
import com.nawaf.capstone3.DTO.AI.MaintenanceAiResponse;
import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleFluidsResponse;
import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleMaintenanceResponse;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceRuleService {
    private final MaintenanceRuleRepository maintenanceRuleRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleDatabaseClient vehicleDatabaseClient;
    private final OpenRouterClient openRouterClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<MaintenanceRule> getMaintenanceRules() {
        return maintenanceRuleRepository.findAll();
    }

    public MaintenanceRule getMaintenanceRuleById(Integer id) {
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(id);
        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        return maintenanceRule;
    }

    public void addMaintenanceRule(Integer vehicleId, MaintenanceRule maintenanceRule) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        if (vehicle == null) throw new ApiException("Vehicle not found");

        maintenanceRule.setVehicle(vehicle);
        maintenanceRuleRepository.save(maintenanceRule);
    }

    public void updateMaintenanceRule(Integer id, MaintenanceRule updateMaintenanceRule) {
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(id);
        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        maintenanceRule.setServiceName(updateMaintenanceRule.getServiceName());
        maintenanceRule.setDescription(updateMaintenanceRule.getDescription());
        maintenanceRule.setCategory(updateMaintenanceRule.getCategory());
        maintenanceRule.setAction(updateMaintenanceRule.getAction());
        maintenanceRule.setKilometers(updateMaintenanceRule.getKilometers());
        maintenanceRule.setMonthInterval(updateMaintenanceRule.getMonthInterval());
        maintenanceRule.setCondition(updateMaintenanceRule.getCondition());
        maintenanceRule.setSpecification(updateMaintenanceRule.getSpecification());
        maintenanceRule.setCapacity(updateMaintenanceRule.getCapacity());
        maintenanceRule.setNotes(updateMaintenanceRule.getNotes());
        maintenanceRule.setSource(updateMaintenanceRule.getSource());

        maintenanceRuleRepository.save(maintenanceRule);
    }

    public void deleteMaintenanceRule(Integer id) {
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(id);
        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        maintenanceRuleRepository.delete(maintenanceRule);
    }
}
