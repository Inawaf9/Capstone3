package com.nawaf.capstone3.DTO;

import com.nawaf.capstone3.Enum.TriggerType;

public record MaintenanceRuleAiDto(
        String serviceName,
        String description,
        TriggerType triggerType,
        Integer kilometerInterval,
        Integer monthInterval,
        String condition,
        String notes) {}