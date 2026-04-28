package br.com.cleanprosolutions.auth.service.impl;

import br.com.cleanprosolutions.auth.document.UserCredential;
import br.com.cleanprosolutions.auth.dto.AuthResponse;
import br.com.cleanprosolutions.auth.dto.LoginRequest;
import br.com.cleanprosolutions.auth.dto.RefreshTokenRequest;
import br.com.cleanprosolutions.auth.dto.RegisterRequest;
import br.com.cleanprosolutions.auth.dto.TokenValidationResponse;
import br.com.cleanprosolutions.auth.exception.InvalidCredentialsException;
import br.com.cleanprosolutions.auth.exception.TokenExpiredException;
import br.com.cleanprosolutions.auth.exception.UserAlreadyExistsException;
import br.com.cleanprosolutions.auth.repository.UserCredentialRepository;
import br.com.cleanprosolutions.auth.service.AuthService;
import br.com.cleanprosolutions.auth.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Implementation of the {@link AuthService} interface.
 *
 * <p>Orchestrates user registration, authentication, token refresh, and token validation.
 * Passwords are never stored in plain text — BCrypt is used for hashing.</p>
 *
 * <p>Refresh token rotation is applied on every refresh, invalidating the previous token.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialRepository repository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     *
     * <p>Checks for duplicate email before persisting the new credential.</p>
     */
    @Override
    public void register(final RegisterRequest request) {
        log.info("Registering new user — email: {}", request.email());

        if (repository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        final UserCredential credential = UserCredential.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        repository.save(credential);
        log.info("User registered successfully — email: {}", request.email());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifies that the account is active and the password matches before issuing tokens.
     * The refresh token is persisted for future rotation.</p>
     */
    @Override
    public AuthResponse login(final LoginRequest request) {
        log.info("Authentication attempt — email: {}", request.email());

        final UserCredential credential = repository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!credential.isActive()) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        final String accessToken = tokenService.generateAccessToken(
                credential.getId(), credential.getEmail(), credential.getRole().name());
        final String refreshToken = tokenService.generateRefreshToken(credential.getId());

        credential.setRefreshToken(refreshToken);
        credential.setUpdatedAt(Instant.now());
        repository.save(credential);

        log.info("User authenticated successfully — email: {}", request.email());
        return AuthResponse.of(accessToken, refreshToken, tokenService.getExpirationInSeconds());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Applies refresh token rotation: the old token is replaced with a new one,
     * preventing replay attacks.</p>
     */
    @Override
    public AuthResponse refresh(final RefreshTokenRequest request) {
        log.info("Token refresh requested");

        if (!tokenService.isValid(request.refreshToken())) {
            throw new TokenExpiredException("Refresh token is invalid or expired");
        }

        final UserCredential credential = repository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() -> new TokenExpiredException("Refresh token not found or already invalidated"));

        final String newAccessToken = tokenService.generateAccessToken(
                credential.getId(), credential.getEmail(), credential.getRole().name());
        final String newRefreshToken = tokenService.generateRefreshToken(credential.getId());

        credential.setRefreshToken(newRefreshToken);
        credential.setUpdatedAt(Instant.now());
        repository.save(credential);

        log.info("Token refreshed for user — email: {}", credential.getEmail());
        return AuthResponse.of(newAccessToken, newRefreshToken, tokenService.getExpirationInSeconds());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Never throws — returns a response with {@code valid=false} in case of any error.</p>
     */
    @Override
    public TokenValidationResponse validate(final String token) {
        try {
            final Claims claims = tokenService.extractClaims(token);
            return new TokenValidationResponse(
                    true,
                    claims.get("email", String.class),
                    claims.get("role", String.class),
                    claims.getSubject()
            );
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return TokenValidationResponse.invalid();
        }
    }
}
