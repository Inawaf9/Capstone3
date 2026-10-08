package com.nawaf.capstone3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "user",
        check = {
                @CheckConstraint(
                        name = "chk_phone_number",
                        constraint = "phone_number REGEXP '^05[0-9]{8}$'"
                )
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Name is required")
    @Size(max = 50, message = "Name cannot be more than 50 characters")
    @Column(nullable = false, length = 50)
    private String name;

    @NotEmpty(message = "Email is required")
    @Email(message = "Enter a valid email")
    @Column(nullable = false, unique = true)
    private String email;

    @NotEmpty(message = "Phone number is required")
    @Pattern(
            regexp = "^05\\d{8}$",
            message = "Phone number must be a valid Saudi number starting with 05"
    )
    @Column(nullable = false, unique = true, length = 10)
    private String phoneNumber;

    @NotEmpty(message = "Password is required")
    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Vehicle> vehicles = new java.util.LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Notification> notifications = new java.util.LinkedHashSet<>();
}
