package com.nawaf.capstone3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Pattern(
            regexp = "^[A-HJ-NPR-Z0-9]{17}$",
            message = "VIN must be exactly 17 valid characters"
    )
    @Column(length = 17, unique = true)
    private String vin;

    @NotEmpty(message = "Make is required")
    @Size(max = 50, message = "Make cannot be more than 50 characters")
    @Column(nullable = false, length = 50)
    private String make;

    @NotEmpty(message = "Model is required")
    @Size(max = 50, message = "Model cannot be more than 50 characters")
    @Column(nullable = false, length = 50)
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1886, message = "Year must be 1886 or later")
    @Column(nullable = false)
    private Integer year;

    @Size(max = 50, message = "Engine cannot be more than 50 characters")
    @Column(length = 50)
    private String engine;

    @Size(max = 20, message = "Fuel type cannot be more than 20 characters")
    @Column(length = 20)
    private String fuelType;

    @PositiveOrZero(message = "Current kilometers cannot be negative")
    private Integer currentKilometers;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "vehicle", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<KilometerRecord> kilometerRecords = new java.util.LinkedHashSet<>();

    @OneToMany(mappedBy = "vehicle", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<AiChatHistory> aiChatHistories = new java.util.LinkedHashSet<>();

    @OneToMany(mappedBy = "vehicle", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<MaintenanceRecord> maintenanceRecords = new java.util.LinkedHashSet<>();

    @OneToMany(mappedBy = "vehicle", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<MaintenanceRule> maintenanceRules = new java.util.LinkedHashSet<>();
    @AssertTrue(message = "Vehicle year cannot exceed next year")
    @JsonIgnore
    public boolean isYearValid() {
        return year == null || year <= java.time.LocalDate.now().getYear() + 1;
    }
}
