package com.nawaf.capstone3.DTO.AI;

import java.util.List;

public record MaintenanceAiResponse(
        List<MaintenanceAiRule> rules
) {}
