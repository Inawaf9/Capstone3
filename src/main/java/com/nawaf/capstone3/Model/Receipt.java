
        package com.nawaf.capstone3.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Setter
@Getter
@Entity
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int")
    private Integer id;

    @NotBlank(message = "Receipt file URL is required")
    @Size(max = 2048, message = "Receipt file URL is too long")
    @Column(columnDefinition = "varchar(2048) not null")
    private String fileUrl;

    @NotNull(message = "Total amount is required")
    @PositiveOrZero(message = "Total amount cannot be negative")
    @Column(columnDefinition = "double not null")
    private Double totalAmount;

    @NotNull(message = "Receipt date is required")
    @Column(columnDefinition = "date not null")
    private LocalDate extractedDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate uploadedAt;
}