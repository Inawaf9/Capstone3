package com.nawaf.capstone3.DTO.AI;

import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleFluidsResponse;
import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleMaintenanceResponse;
import java.util.List;

public record MaintenanceAiRequest(
        VehicleMaintenanceResponse maintenance,
        VehicleFluidsResponse fluids,
        List<ScheduledService> scheduledServices
) {
    public record ScheduledService(int index, String service, int kilometers) {}
}
