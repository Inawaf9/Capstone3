package com.nawaf.capstone3.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CompleteMaintenanceDTO(
        @NotNull @PositiveOrZero Integer kilometers,
        @NotNull @PositiveOrZero Double cost,
        @Size(max = 150) String workshop,
        @Size(max = 1000) String note) {
}
