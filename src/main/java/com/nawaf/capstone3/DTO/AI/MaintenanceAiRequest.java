package com.nawaf.capstone3.DTO.AI;

import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleFluidsResponse;
import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleMaintenanceResponse;

public record MaintenanceAiRequest(
        VehicleMaintenanceResponse maintenance,
        VehicleFluidsResponse fluids
) {}