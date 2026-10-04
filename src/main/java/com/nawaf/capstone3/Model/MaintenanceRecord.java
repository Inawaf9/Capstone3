package com.nawaf.capstone3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Kilometers are required")
    @PositiveOrZero(message = "Kilometers cannot be negative")
    @Column(nullable = false)
    private Integer kilometers;

    @NotNull(message = "Service date is required")
    @Column(nullable = false)
    private LocalDate serviceDate;

    @NotNull(message = "Cost is required")
    @PositiveOrZero(message = "Cost cannot be negative")
    @Column(nullable = false)
    private Double cost;

    @Size(max = 150, message = "Workshop name must not exceed 150 characters")
    @Column(length = 150)
    private String workshop;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    @Column(length = 1000)
    private String note;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "maintenance_rule_id", nullable = false)
    @JsonIgnore
    private MaintenanceRule maintenanceRule;

    @OneToMany(mappedBy = "maintenanceRecord", cascade = CascadeType.ALL)
    private Set<Receipt> receipts;

    @OneToMany(mappedBy = "maintenanceRecord", cascade = CascadeType.ALL)
    private Set<Notification> notifications;
}