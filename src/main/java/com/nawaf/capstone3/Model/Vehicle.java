package com.nawaf.capstone3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @NotEmpty(message = "Make required")
    @Size(max = 50, message = "Make cannot be more than 50 characters")
    @Column(nullable = false, length = 50)
    private String make;

    @NotEmpty(message = "Model required")
    @Size(max = 50, message = "Model cannot be more than 50 characters")
    @Column(nullable = false, length = 50)
    private String model;

    @NotNull(message = "Year required")
    @Positive(message = "Year must be positive number")
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private Set<KilometerRecord> kilometerRecords;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private Set<AiChatHistory> aiChatHistories;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private Set<MaintenanceRecord> maintenanceRecords;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private Set<UserManual> userManuals;
}