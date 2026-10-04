package com.nawaf.capstone3.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class KilometerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Kilometers required")
    @Positive(message = "Kilometers must be positive number")
    @Column(nullable = false)
    private Integer kilometers;

    @Size(max = 200, message = "Note cannot be more than 200 characters")
    @Column
    private String note;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime recordedAt;
}
