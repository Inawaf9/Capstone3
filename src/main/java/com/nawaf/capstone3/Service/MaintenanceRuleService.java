package com.nawaf.capstone3.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Client.OpenRouterClient;
import com.nawaf.capstone3.Client.VehicleDatabaseClient;
import com.nawaf.capstone3.DTO.AI.MaintenanceAiRequest;
import com.nawaf.capstone3.DTO.AI.MaintenanceAiResponse;
import com.nawaf.capstone3.DTO.AI.MaintenanceAiRule;
import com.nawaf.capstone3.DTO.AI.MaintenanceAiRequest.ScheduledService;
import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleMaintenanceResponse;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import java.util.List;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;
import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;

@Service
@RequiredArgsConstructor
public class MaintenanceRuleService {
    private final jakarta.persistence.EntityManager entityManager;
    private static final int AI_BATCH_SIZE = 3;
    private final MaintenanceRuleRepository maintenanceRuleRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleDatabaseClient vehicleDatabaseClient;
    private final OpenRouterClient openRouterClient;
    private final Validator validator;
    private final TransactionTemplate transactionTemplate;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<MaintenanceRule> getMaintenanceRules() {
        return maintenanceRuleRepository.findAll();
    }

    public List<MaintenanceRule> getRuleByVehicleId(Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        if (vehicle == null) throw new ApiException("Vehicle not found");
        return maintenanceRuleRepository.findMaintenanceRuleByVehicle(vehicle);
    }

    public MaintenanceRule getMaintenanceRuleById(Integer id) {
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(id);
        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        return maintenanceRule;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void addMaintenanceRule(Integer vehicleId, MaintenanceRule maintenanceRule) {
        Vehicle vehicle = findFreshVehicleForUpdate(vehicleId);
        if (vehicle == null) throw new ApiException("Vehicle not found");

        normalizeAndValidate(maintenanceRule);
        rejectDuplicate(vehicle, maintenanceRule, null);
        maintenanceRule.setId(null);
        maintenanceRule.setMaintenanceRecords(new java.util.LinkedHashSet<>());
        maintenanceRule.setVehicle(vehicle);
        maintenanceRuleRepository.save(maintenanceRule);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void updateMaintenanceRule(Integer id, MaintenanceRule updateMaintenanceRule) {
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(id);
        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        findFreshVehicleForUpdate(maintenanceRule.getVehicle().getId());
        maintenanceRule = findFreshRuleForUpdate(id);
        normalizeAndValidate(updateMaintenanceRule);
        if (maintenanceRecordRepository.existsByMaintenanceRuleId(id)
                && !key(maintenanceRule).equals(key(updateMaintenanceRule)))
            throw new ApiException("Cannot change the schedule of a rule with completed history");
        rejectDuplicate(maintenanceRule.getVehicle(), updateMaintenanceRule, id);
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

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void deleteMaintenanceRule(Integer id) {
        MaintenanceRule maintenanceRule = maintenanceRuleRepository.findMaintenanceRuleById(id);
        if (maintenanceRule == null) throw new ApiException("Maintenance rule not found");

        findFreshVehicleForUpdate(maintenanceRule.getVehicle().getId());
        maintenanceRule = findFreshRuleForUpdate(id);
        if (maintenanceRecordRepository.existsByMaintenanceRuleId(id))
            throw new ApiException("Maintenance rule is referenced by completed history");
        maintenanceRuleRepository.delete(maintenanceRule);
    }

    public List<MaintenanceRule> analyzeVehicle(Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        if (vehicle == null) throw new ApiException("Vehicle not found");
        String vin = vehicle.getVin();
        if (vin == null || !vin.matches("^[A-HJ-NPR-Z0-9]{17}$")) {
            throw new ApiException("Vehicle must have a valid VIN before maintenance analysis");
        }
        VehicleMaintenanceResponse maintenance = vehicleDatabaseClient.getMaintenance(vin);
        List<ScheduledService> schedule = validatedSchedule(maintenance, vin);
        // Fluids access is optional and deliberately not requested by this workflow.
        try {
            // Bound structured output size without dropping later scheduled mileages.
            Map<RuleKey, MaintenanceRule> normalized = new LinkedHashMap<>();
            for (int start = 0; start < schedule.size(); start += AI_BATCH_SIZE) {
                List<ScheduledService> batch = new ArrayList<>();
                for (ScheduledService item : schedule.subList(start, Math.min(start + AI_BATCH_SIZE, schedule.size()))) {
                    batch.add(new ScheduledService(batch.size(), item.service(), item.kilometers()));
                }
                var data = maintenance.data();
                var batchMaintenance = new VehicleMaintenanceResponse(maintenance.status(),
                        new VehicleMaintenanceResponse.MaintenanceData(data.vin(), data.year(), data.make(), data.model(), data.trim(),
                                batch.stream().map(item -> new VehicleMaintenanceResponse.MaintenanceEntry(
                                        new VehicleMaintenanceResponse.Mileage(null, item.kilometers()), List.of(item.service()))).toList()));
                String json = objectMapper.writeValueAsString(new MaintenanceAiRequest(batchMaintenance, null, batch));
                MaintenanceAiResponse response = openRouterClient.analyzeText(
                        MaintenancePrompts.NORMALIZE_MAINTENANCE, json, MaintenanceAiResponse.class);
                for (MaintenanceRule rule : validatedRules(response, batch)) normalized.putIfAbsent(key(rule), rule);
            }
            List<MaintenanceRule> rules = new ArrayList<>(normalized.values());
            // External calls finish before the short database transaction/vehicle lock.
            // Append only: never delete or replace rules referenced by completed services.
            return transactionTemplate.execute(status -> {
                Vehicle lockedVehicle = findFreshVehicleForUpdate(vehicleId);
                if (lockedVehicle == null || !vin.equals(lockedVehicle.getVin())) {
                    throw new ApiException("Vehicle changed during analysis; please retry");
                }
                Map<RuleKey, MaintenanceRule> existing = new LinkedHashMap<>();
                for (MaintenanceRule rule : maintenanceRuleRepository.findMaintenanceRuleByVehicle(lockedVehicle)) {
                    existing.putIfAbsent(key(rule), rule);
                }
                List<MaintenanceRule> result = new ArrayList<>();
                List<MaintenanceRule> additions = new ArrayList<>();
                for (MaintenanceRule rule : rules) {
                    MaintenanceRule saved = existing.get(key(rule));
                    if (saved == null) {
                        rule.setVehicle(lockedVehicle);
                        additions.add(rule);
                        existing.put(key(rule), rule);
                        saved = rule;
                    }
                    result.add(saved);
                }
                maintenanceRuleRepository.saveAllAndFlush(additions);
                return result;
            });

        } catch (JsonProcessingException exception) {
            throw new ApiException("Failed to process vehicle maintenance data");
        }
    }

    private List<ScheduledService> validatedSchedule(VehicleMaintenanceResponse response, String vin) {
        if (response == null || !"success".equalsIgnoreCase(response.status()) || response.data() == null
                || response.data().vin() == null || !vin.equalsIgnoreCase(response.data().vin())
                || response.data().maintenance() == null || response.data().maintenance().isEmpty()) {
            throw new ApiException("Vehicle Databases returned no successful maintenance schedule for this VIN");
        }
        List<ScheduledService> schedule = new ArrayList<>();
        for (var entry : response.data().maintenance()) {
            if (entry == null || entry.mileage() == null || entry.service_items() == null
                    || entry.service_items().isEmpty()) {
                throw new ApiException("Vehicle Databases returned an incomplete maintenance entry");
            }
            Integer kilometers = entry.mileage().km();
            if (kilometers == null) {
                Integer miles = entry.mileage().miles();
                if (miles == null || miles <= 0) {
                    throw new ApiException("Maintenance entry has no explicit positive mileage");
                }
                try {
                    kilometers = BigDecimal.valueOf(miles).multiply(new BigDecimal("1.609344"))
                            .setScale(0, RoundingMode.HALF_UP).intValueExact();
                } catch (ArithmeticException exception) {
                    throw new ApiException("Maintenance mileage is outside the supported range");
                }
            }
            if (kilometers <= 0) throw new ApiException("Maintenance mileage must be positive");
            for (String item : entry.service_items()) {
                if (item == null || item.isBlank()) throw new ApiException("Maintenance service is empty");
                if (item.length() > 2000) throw new ApiException("Maintenance service text is too long");
                schedule.add(new ScheduledService(schedule.size(), item.strip(), kilometers));
            }
        }
        if (schedule.size() > 1000) throw new ApiException("Maintenance schedule exceeds the supported entry limit");
        return schedule;
    }

    private List<MaintenanceRule> validatedRules(MaintenanceAiResponse response, List<ScheduledService> schedule) {
        if (response == null || response.rules() == null || response.rules().isEmpty()) {
            throw new ApiException("No maintenance rules returned by AI");
        }
        Map<RuleKey, MaintenanceRule> unique = new LinkedHashMap<>();
        Map<Integer, RuleKey> covered = new LinkedHashMap<>();
        for (MaintenanceAiRule aiRule : response.rules()) {
            if (aiRule == null || aiRule.kilometers() == null || aiRule.sourceIndexes() == null
                    || aiRule.sourceIndexes().isEmpty()) {
                throw new ApiException("AI maintenance rule is missing mileage or source references");
            }
            MaintenanceRule rule = new MaintenanceRule();
            rule.setServiceName(clean(aiRule.serviceName()));
            rule.setCategory(upper(aiRule.category()));
            rule.setAction(upper(aiRule.action()));
            rule.setKilometers(aiRule.kilometers());
            rule.setDescription(clean(aiRule.description()));
            rule.setSpecification(clean(aiRule.specification()));
            rule.setCapacity(clean(aiRule.capacity()));
            // Provenance is controlled by the server, not invented by the model.
            rule.setSource("Vehicle Databases Maintenance API");
            if (!validator.validate(rule).isEmpty()) throw new ApiException("AI returned an invalid maintenance rule");
            RuleKey key = key(rule);
            for (Integer index : new HashSet<>(aiRule.sourceIndexes())) {
                if (index == null || index < 0 || index >= schedule.size()
                        || schedule.get(index).kilometers() != aiRule.kilometers()) {
                    throw new ApiException("AI changed a scheduled mileage or returned an unknown source");
                }
                RuleKey previous = covered.putIfAbsent(index, key);
                if (previous != null && !previous.equals(key)) {
                    throw new ApiException("AI returned conflicting rules for the same source entry");
                }
            }
            unique.putIfAbsent(key, rule);
        }
        if (covered.size() != schedule.size()) throw new ApiException("AI omitted maintenance schedule entries");
        return new ArrayList<>(unique.values());
    }

    private void normalizeAndValidate(MaintenanceRule rule) {
        rule.setServiceName(clean(rule.getServiceName()));
        rule.setCategory(upper(rule.getCategory()));
        rule.setAction(upper(rule.getAction()));
        if (!validator.validate(rule).isEmpty()) throw new ApiException("Invalid maintenance rule");
    }

    private void rejectDuplicate(Vehicle vehicle, MaintenanceRule input, Integer excludedId) {
        if (maintenanceRuleRepository.findMaintenanceRuleByVehicle(vehicle).stream()
                .anyMatch(rule -> !Objects.equals(rule.getId(), excludedId) && key(rule).equals(key(input))))
            throw new ApiException("This scheduled maintenance rule already exists");
    }

    private static String clean(String value) {
        return value == null ? null : value.strip().replaceAll("\\s+", " ");
    }

    private static String upper(String value) {
        String clean = clean(value);
        return clean == null ? null : clean.toUpperCase(Locale.ROOT);
    }

    private static RuleKey key(MaintenanceRule rule) {
        return new RuleKey(upper(rule.getServiceName()), upper(rule.getAction()), rule.getKilometers(),
                rule.getMonthInterval(), upper(rule.getCondition()));
    }

    private record RuleKey(String service, String action, Integer kilometers, Integer months, String condition) {}

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
