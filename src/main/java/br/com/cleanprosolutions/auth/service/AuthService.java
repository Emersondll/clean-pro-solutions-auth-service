package br.com.cleanprosolutions.auth.service;

import br.com.cleanprosolutions.auth.dto.AuthResponse;
import br.com.cleanprosolutions.auth.dto.LoginRequest;
import br.com.cleanprosolutions.auth.dto.RefreshTokenRequest;
import br.com.cleanprosolutions.auth.dto.RegisterRequest;
import br.com.cleanprosolutions.auth.dto.TokenValidationResponse;

/**
 * Service interface defining authentication operations.
 *
 * <p>Implementations handle user registration, login, token refresh, and validation.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * Registers a new user in the system.
     *
     * @param request the registration request containing email, password and role
     * @throws br.com.cleanprosolutions.auth.exception.UserAlreadyExistsException if email is taken
     */
    void register(RegisterRequest request);

    /**
     * Authenticates a user and returns JWT access + refresh tokens.
     *
     * @param request the login request with email and password
     * @return {@link AuthResponse} containing the access and refresh tokens
     * @throws br.com.cleanprosolutions.auth.exception.InvalidCredentialsException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param request containing the current refresh token
     * @return {@link AuthResponse} with new access and refresh tokens (rotation)
     * @throws br.com.cleanprosolutions.auth.exception.TokenExpiredException if token is invalid/expired
     */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Validates a JWT access token and extracts user claims.
     *
     * <p>Used by the BFF to verify tokens without holding the signing secret.</p>
     *
     * @param token the JWT token to validate
     * @return {@link TokenValidationResponse} with validity flag and claims
     */
    TokenValidationResponse validate(String token);
}
