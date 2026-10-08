package com.nawaf.capstone3.DTO;

import jakarta.validation.constraints.*;

/** Password is optional on update; service enforces it on registration. */
public record UserRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^05\\d{8}$") String phoneNumber,
        @Size(min = 6, max = 128)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
                message = "Password must contain uppercase, lowercase, number, and special character")
        String password
) {}
