package com.nawaf.capstone3.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateCostDTO(@NotNull @PositiveOrZero Double cost) {
}
