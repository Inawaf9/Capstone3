package com.nawaf.capstone3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "user_manual",
        check = {
                @CheckConstraint(
                        name = "chk_user_manual_status",
                        constraint = "status IN ('UPLOADED','ANALYZING','COMPLETED','FAILED')"
                )
        }
)
public class UserManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "File URL is required")
    @Size(max = 2048, message = "File URL must not exceed 2048 characters")
    @Column(nullable = false, length = 2048)
    private String fileUrl;

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "^(UPLOADED|ANALYZING|COMPLETED|FAILED)$",
            message = "Invalid manual status"
    )
    @Column(nullable = false, length = 20)
    private String status = "UPLOADED";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    private Vehicle vehicle;

    @OneToMany(mappedBy = "userManual", cascade = CascadeType.ALL)
    private Set<MaintenanceRule> maintenanceRules;
}