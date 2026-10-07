package com.nawaf.capstone3.DTO.VehicleDatabase;

import java.util.List;

public record VehicleFluidsResponse(
        String status,
        String vin,
        Integer year,
        String make,
        String model,
        String trim,
        List<FluidSection> data
) {
    public record FluidSection(
            String title,
            List<FluidItem> items
    ) {}

    public record FluidItem(
            String key,
            String value
    ) {}
}