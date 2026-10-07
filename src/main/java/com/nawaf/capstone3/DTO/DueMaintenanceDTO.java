package com.nawaf.capstone3.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DueMaintenanceDTO {
    private Integer ruleId;
    private String serviceName;
    private Integer dueKm;
    private Integer currentKm;
    private Integer remainingKm; // إذا كان بالسالب يعكس حجم التأخير (Overdue)
    private String status;       // "DUE_NOW" أو "OVERDUE"
}