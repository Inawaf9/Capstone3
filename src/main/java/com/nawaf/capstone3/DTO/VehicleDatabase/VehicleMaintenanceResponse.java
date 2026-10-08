package com.nawaf.capstone3.DTO.VehicleDatabase;

import java.util.List;

public record VehicleMaintenanceResponse(
        String status,
        MaintenanceData data
) {
    public record MaintenanceData(
            String vin,
            Integer year,
            String make,
            String model,
            String trim,
            List<MaintenanceEntry> maintenance
    ) {}

    public record MaintenanceEntry(
            Mileage mileage,
            List<String> service_items
    ) {}

    public record Mileage(
            Integer miles,
            Integer km
    ) {}
}
