package br.com.cleanprosolutions.auth.controller;

import br.com.cleanprosolutions.auth.dto.AuthResponse;
import br.com.cleanprosolutions.auth.dto.LoginRequest;
import br.com.cleanprosolutions.auth.dto.RefreshTokenRequest;
import br.com.cleanprosolutions.auth.dto.RegisterRequest;
import br.com.cleanprosolutions.auth.dto.TokenValidationResponse;
import br.com.cleanprosolutions.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing authentication endpoints.
 *
 * <p>All business logic is delegated to {@link AuthService}.
 * Controller responsibilities are limited to HTTP handling and logging.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, token refresh and validation")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user in the system.
     *
     * @param request registration payload (email, password, role)
     * @return 201 Created on success
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<Void> register(@Valid @RequestBody final RegisterRequest request) {
        log.info("POST /auth/register — email: {}", request.email());
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Authenticates a user and returns JWT tokens.
     *
     * @param request login payload (email, password)
     * @return 200 OK with access and refresh tokens
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody final LoginRequest request) {
        log.info("POST /auth/login — email: {}", request.email());
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param request refresh token payload
     * @return 200 OK with new access and refresh tokens
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a valid refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalid or expired")
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody final RefreshTokenRequest request) {
        log.info("POST /auth/refresh");
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * Validates a JWT access token and returns extracted user claims.
     *
     * <p>This endpoint is used internally by the BFF to verify tokens
     * without needing access to the JWT signing secret.</p>
     *
     * @param authHeader the {@code Authorization: Bearer <token>} header
     * @return 200 OK with validation result and claims
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token and extract user claims (BFF internal use)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation result returned"),
            @ApiResponse(responseCode = "400", description = "Missing Authorization header")
    })
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestHeader("Authorization") final String authHeader) {
        log.info("GET /auth/validate");
        final String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(authService.validate(token));
    }
}
