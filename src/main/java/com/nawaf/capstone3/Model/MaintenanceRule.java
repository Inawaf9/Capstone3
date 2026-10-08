package com.nawaf.capstone3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @NotBlank(message = "Category is required")
    @Pattern(
            regexp = "^(ENGINE|TRANSMISSION|BRAKES|COOLING|AIR_FILTER|CABIN_FILTER|TIRES|STEERING|SUSPENSION|ELECTRICAL|FUEL|OTHER)$",
            message = "Invalid maintenance category"
    )
    @Column(nullable = false, length = 30)
    private String category;

    @NotBlank(message = "Action is required")
    @Pattern(
            regexp = "^(REPLACE|INSPECT|CHANGE|CHECK|SERVICE|OTHER)$",
            message = "Invalid maintenance action"
    )
    @Column(nullable = false, length = 20)
    private String action;

    @Positive(message = "Kilometers must be positive")
    private Integer kilometers;

    @Positive(message = "Month interval must be positive")
    private Integer monthInterval;

    @Size(max = 500, message = "Condition must not exceed 500 characters")
<<<<<<< Updated upstream
    @Column(name = "maintenance_condition", length = 500)
=======
    @Column(length = 500,name = "rule_condition")//عدلت الاسم عشان ما يكون فيه مشكلة بقاعدة البيانات
>>>>>>> Stashed changes
    private String condition;

    @Size(max = 500, message = "Specification must not exceed 500 characters")
    @Column(length = 500)
    private String specification;

    @Size(max = 100, message = "Capacity must not exceed 100 characters")
    @Column(length = 100)
    private String capacity;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    @Size(max = 100, message = "Source must not exceed 100 characters")
    @Column(length = 100)
    private String source;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    private Vehicle vehicle;

    @OneToMany(mappedBy = "maintenanceRule")
    @JsonIgnore//هيصير تكرار لانهائي لو حذفناها داخل تحليل ال Ai
    private Set<MaintenanceRecord> maintenanceRecords;


}