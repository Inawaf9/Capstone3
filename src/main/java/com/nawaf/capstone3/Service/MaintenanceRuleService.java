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

    public List<MaintenanceRule> analyzeVehicle(Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        if (vehicle == null) throw new ApiException("Vehicle not found");

        VehicleMaintenanceResponse maintenance = vehicleDatabaseClient.getMaintenance(vehicle.getVin());

        MaintenanceAiRequest request = new MaintenanceAiRequest(maintenance, null);

        try {
            String json = objectMapper.writeValueAsString(request);

            MaintenanceAiResponse response = openRouterClient.analyzeText(
                    MaintenancePrompts.NORMALIZE_MAINTENANCE,
                    json,
                    MaintenanceAiResponse.class
            );

            if (response == null || response.rules() == null || response.rules().isEmpty())
                throw new ApiException("No maintenance rules found");

            List<MaintenanceRule> rules = response.rules().stream()
                    .map(aiRule -> {
                        MaintenanceRule rule = new MaintenanceRule();
                        rule.setServiceName(aiRule.serviceName());
                        rule.setDescription(aiRule.description());
                        rule.setCategory(aiRule.category());
                        rule.setAction(aiRule.action());
                        rule.setKilometers(aiRule.kilometers());
                        rule.setSpecification(aiRule.specification());
                        rule.setCapacity(aiRule.capacity());
                        rule.setSource(aiRule.sources() == null ? null : String.join(", ", aiRule.sources()));
                        rule.setVehicle(vehicle);

                        return rule;
                    })
                    .toList();

            return maintenanceRuleRepository.saveAll(rules);

        } catch (JsonProcessingException exception) {
            throw new ApiException("Failed to process vehicle maintenance data");
        }
    }
}