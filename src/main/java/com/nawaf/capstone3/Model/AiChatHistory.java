package com.nawaf.capstone3.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
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
public class AiChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "User message require")
    @Size(max = 200, message = "User message cannot more than 200 characters")
    @Column(nullable = false, length = 200)
    private String userMessage;

    @NotEmpty(message = "Ai response required")
    @Column(nullable = false)
    private String aiResponse;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
