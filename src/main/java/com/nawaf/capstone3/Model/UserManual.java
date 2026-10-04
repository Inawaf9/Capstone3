
        package com.nawaf.capstone3.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class UserManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int")
    private Integer id;

    @NotBlank(message = "File URL is required")
    @Size(max = 2048, message = "File URL must not exceed 2048 characters")
    @Column(columnDefinition = "varchar(2048) not null")
    private String fileUrl;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(UPLOADED|ANALYZING|COMPLETED|FAILED)$", message = "Invalid manual status")
    @Column(columnDefinition = "varchar(20) not null check (status in ('UPLOADED','ANALYZING','COMPLETED','FAILED'))")
    private String status;

    @CreationTimestamp
    @Column( updatable = false )
    private LocalDateTime uploadedAt;
}