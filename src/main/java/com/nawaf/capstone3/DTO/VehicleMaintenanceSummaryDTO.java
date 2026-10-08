package com.nawaf.capstone3.DTO;

import com.nawaf.capstone3.Model.MaintenanceRecord;
import java.util.List;

public record VehicleMaintenanceSummaryDTO(Integer vehicleId, String make, String model,
        Integer currentKilometers, int totalRecords, List<MaintenanceRecord> records) {
}
