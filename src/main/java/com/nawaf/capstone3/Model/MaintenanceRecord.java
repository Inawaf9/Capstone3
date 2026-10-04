
        package com.nawaf.capstone3.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int")
    private Integer id;

    @NotNull(message = "Kilometers are required")
    @PositiveOrZero(message = "Kilometers cannot be negative")
    @Column(columnDefinition = "int not null")
    private Integer kilometers;

    @NotNull(message = "Service date is required")
    @Column(columnDefinition = "date not null")
    private LocalDate serviceDate;

    @NotNull(message = "Cost is required")
    @PositiveOrZero(message = "Cost cannot be negative")
    @Column(columnDefinition = "double not null")
    private Double cost;

    @Size(max = 150, message = "Workshop name must not exceed 150 characters")
    @Column(columnDefinition = "varchar(150)")
    private String workshop;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    @Column(columnDefinition = "varchar(1000)")
    private String note;
}