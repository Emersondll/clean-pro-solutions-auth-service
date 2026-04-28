package br.com.cleanprosolutions.auth.dto;

import br.com.cleanprosolutions.auth.enumerations.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration.
 *
 * @param email    user email address (must be unique and valid format)
 * @param password user password (minimum 8 characters)
 * @param role     user role in the platform (CLIENT, CONTRACTOR, or ADMIN)
 */
public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must have at least 8 characters")
        String password,

        @NotNull(message = "Role is required")
        UserRole role
) {}
