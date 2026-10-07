package com.nawaf.capstone3.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDTO {

    private Double totalAmount;
    private LocalDate extractedDate;
    private String serviceType;
}