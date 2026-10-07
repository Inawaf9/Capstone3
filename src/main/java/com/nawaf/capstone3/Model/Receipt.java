package com.nawaf.capstone3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Total amount is required")
    @PositiveOrZero(message = "Total amount cannot be negative")
    @Column(nullable = false)
    private Double totalAmount;

    @NotNull(message = "Receipt date is required")
    @Column(nullable = false)
    private LocalDate extractedDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "maintenance_record_id", nullable = false)
    @JsonIgnore
    private MaintenanceRecord maintenanceRecord;
}