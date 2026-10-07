package com.nawaf.capstone3.DTO.AI;

import java.util.List;

public record MaintenanceAiRule(
        String serviceName,
        String category,
        String action,
        Integer kilometers,
        String description,
        String specification,
        String capacity,
        List<String> sources
) {}