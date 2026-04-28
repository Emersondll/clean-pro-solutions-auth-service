package br.com.cleanprosolutions.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing an access token.
 *
 * @param refreshToken the valid refresh token to exchange for a new access token
 */
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
