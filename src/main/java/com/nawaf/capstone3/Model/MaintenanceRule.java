
        package com.nawaf.capstone3.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
public class MaintenanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int")
    private Integer id;

    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name must not exceed 100 characters")
    @Column(columnDefinition = "varchar(100) not null")
    private String serviceName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(columnDefinition = "varchar(500)")
    private String description;

    @NotBlank(message = "Trigger type is required")
    @Pattern(
            regexp = "^(KILOMETER|TIME|KILOMETER OR TIME|CONDITION)$",
            message = "Invalid trigger type"
    )
    @Column(
            columnDefinition = "varchar(30) not null check (trigger_type in ('KILOMETER','TIME','KILOMETER OR TIME','CONDITION'))"
    )
    private String triggerType;

    @Positive(message = "Kilometer interval must be positive")
    @Column(columnDefinition = "int")
    private Integer kilometerInterval;

    @Positive(message = "Month interval must be positive")
    @NotNull(message = "Month interval is required")
    @Column(columnDefinition = "int not null")
    private Integer monthInterval;

    @Size(max = 500, message = "Condition must not exceed 500 characters")
    @Column(columnDefinition = "varchar(500)")
    private String condition;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(columnDefinition = "varchar(1000)")
    private String notes;
}