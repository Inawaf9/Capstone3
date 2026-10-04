package com.nawaf.capstone3.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    @Column(length = 17)
    private String vin;

    @NotEmpty(message = "Make require")
    @Size(max = 50, message = "Make cannot more than 50 characters")
    @Column(nullable = false, length = 50)
    private String make;

    @NotEmpty(message = "Model require")
    @Size(max = 50, message = "Model cannot more than 50 characters")
    @Column(nullable = false, length = 50)
    private String Model;

    @NotNull(message = "Year required")
    @Positive(message = "Year must be positive number")
    @Column(nullable = false)
    private Integer year;

    @Size(max = 50, message = "Engine cannot more than 50 characters")
    @Column(length = 50)
    private String engine;

    @Size(max = 20, message = "Fuel type cannot more than 20 characters")
    @Column(length = 50)
    private String fuelType;


    @Positive(message = "Current kilo meters must be positive")
    @Column
    private Integer currentKilometers;
}
