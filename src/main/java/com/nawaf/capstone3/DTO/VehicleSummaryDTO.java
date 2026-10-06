package com.nawaf.capstone3.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleSummaryDTO {

    private Integer vehicleId;
    private String vehicle;
    private Integer year;
    private Integer currentKilometers;
    private Integer vehicleAge;
    private Integer totalKilometerRecords;
}