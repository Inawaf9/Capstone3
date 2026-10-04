package com.nawaf.capstone3.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "notification",
        check = {
                @CheckConstraint(
                        name = "chk_notification_type",
                        constraint = "type IN ('MAINTENANCE_DUE', 'MAINTENANCE_SOON', 'MAINTENANCE_OVERDUE', 'REPORT')"
                ),
                @CheckConstraint(
                        name = "chk_notification_channel",
                        constraint = "channel IN ('WHATSAPP', 'EMAIL')"
                ),
                @CheckConstraint(
                        name = "chk_notification_status",
                        constraint = "status IN ('PENDING', 'SENT', 'FAILED')"
                )
        }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Notification type is required")
    @Pattern(
            regexp = "^(MAINTENANCE_DUE|MAINTENANCE_SOON|MAINTENANCE_OVERDUE|REPORT)$",
            message = "Type must be MAINTENANCE_DUE, MAINTENANCE_SOON, MAINTENANCE_OVERDUE, or REPORT"
    )
    @Column(nullable = false)
    private String type;

    @NotEmpty(message = "Notification channel is required")
    @Pattern(
            regexp = "^(WHATSAPP|EMAIL)$",
            message = "Channel must be WHATSAPP or EMAIL"
    )
    @Column(nullable = false)
    private String channel;

    @NotEmpty(message = "Message required")
    @Size(max = 100, message = "Message cannot be more than 100 characters")
    @Column(nullable = false, length = 100)
    private String message;

    @Pattern(
            regexp = "^(PENDING|SENT|FAILED)$",
            message = "Status must be PENDING, SENT, or FAILED"
    )
    @Column(nullable = false)
    private String status = "PENDING";
    private LocalDateTime sentAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
