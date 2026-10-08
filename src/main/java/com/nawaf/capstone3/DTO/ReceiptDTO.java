package com.nawaf.capstone3.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDTO {

    @NotNull @PositiveOrZero
    private Double totalAmount;
    @NotNull @PastOrPresent
    private LocalDate extractedDate;
    @Size(max = 100)
    private List<@NotBlank @Size(max = 200) String> services;
}
