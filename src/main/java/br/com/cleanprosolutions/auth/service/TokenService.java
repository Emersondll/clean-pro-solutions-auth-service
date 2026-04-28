package br.com.cleanprosolutions.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for JWT token generation and validation.
 *
 * <p>Generates signed HS256 tokens using secrets from environment variables.
 * Both access tokens (short-lived) and refresh tokens (long-lived) are supported.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class TokenService {

    private final SecretKey secretKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    /**
     * Constructs the TokenService with JWT configuration from environment.
     *
     * @param secret             base64 or plain-text secret for HMAC signing
     * @param expirationMs       access token expiration in milliseconds
     * @param refreshExpirationMs refresh token expiration in milliseconds
     */
    public TokenService(
            @Value("${jwt.secret}") final String secret,
            @Value("${jwt.expiration}") final long expirationMs,
            @Value("${jwt.refresh-expiration}") final long refreshExpirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /**
     * Generates a JWT access token containing user identity claims.
     *
     * @param userId user unique identifier (subject)
     * @param email  user email address
     * @param role   user role name
     * @return signed compact JWT string
     */
    public String generateAccessToken(final String userId, final String email, final String role) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claims(Map.of("email", email, "role", role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generates a JWT refresh token with a unique JTI for one-time use tracking.
     *
     * @param userId user unique identifier (subject)
     * @return signed compact JWT refresh token string
     */
    public String generateRefreshToken(final String userId) {
        final Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshExpirationMs)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Parses and returns all claims from a JWT token.
     *
     * @param token the compact JWT string
     * @return the parsed {@link Claims} payload
     * @throws JwtException if the token is malformed, expired, or has invalid signature
     */
    public Claims extractClaims(final String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks whether a token is valid (correctly signed and not expired).
     *
     * @param token the compact JWT string to validate
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean isValid(final String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Returns the access token expiration time in seconds.
     *
     * @return expiration in seconds (e.g., 900 for 15 minutes)
     */
    public long getExpirationInSeconds() {
        return expirationMs / 1000L;
    }
}
