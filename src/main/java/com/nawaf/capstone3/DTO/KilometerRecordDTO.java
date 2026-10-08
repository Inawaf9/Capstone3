package com.nawaf.capstone3.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KilometerRecordDTO {

    @NotNull(message = "Kilometers are required")
    @PositiveOrZero(message = "Kilometers cannot be negative")
    private Integer kilometers;

    @Size(max = 200, message = "Note cannot be more than 200 characters")
    private String note;
}
