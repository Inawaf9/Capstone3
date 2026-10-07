package com.nawaf.capstone3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MaintenanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String serviceName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotBlank(message = "Trigger type is required")
    @Pattern(
            regexp = "^(KILOMETER|TIME|KILOMETER_OR_TIME|CONDITION)$",
            message = "Invalid trigger type"
    )
    @Column(nullable = false, length = 30)
    private String triggerType;

    @Positive(message = "Kilometer interval must be positive")
    private Integer kilometerInterval;

    @Positive(message = "Month interval must be positive")
    private Integer monthInterval;

    @Size(max = 500, message = "Condition must not exceed 500 characters")
    @Column(name = "maintenance_condition", length = 500)
    private String condition;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "user_manual_id", nullable = false)
    @JsonIgnore
    private UserManual userManual;

    @OneToMany(mappedBy = "maintenanceRule")
    private Set<MaintenanceRecord> maintenanceRecords;
}