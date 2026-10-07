package com.nawaf.capstone3.DTO;

import com.nawaf.capstone3.Model.MaintenanceRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleMaintenanceSummaryDTO {
    private Integer vehicleId;
    private String make;
    private String model;
    private Integer currentKilometers;
    private Integer totalMaintenanceRecords;
    private List maintenanceRecords;
}