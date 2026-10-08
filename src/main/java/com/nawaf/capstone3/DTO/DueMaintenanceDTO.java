package com.nawaf.capstone3.DTO;

public record DueMaintenanceDTO(Integer maintenanceRuleId, String serviceName, Integer dueKilometers,
        Integer currentKilometers, long remainingKilometers, String status) {
}
