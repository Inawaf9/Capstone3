package com.nawaf.capstone3.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompleteMaintenanceDTO {



    @NotNull(message = "Kilometers are required")
    @PositiveOrZero(message = "Kilometers cannot be negative")
    private Integer kilometers;

    @NotNull(message = "Cost is required")
    @PositiveOrZero(message = "Cost cannot be negative")
    private Double cost;

    @Size(max = 150, message = "Workshop name must not exceed 150 characters")
    private String workshop;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
}