package br.com.cleanprosolutions.auth.dto;

/**
 * Response DTO for successful authentication operations.
 *
 * @param accessToken  JWT access token (valid for 15 minutes)
 * @param refreshToken JWT refresh token (valid for 7 days)
 * @param tokenType    token type, always {@code "Bearer"}
 * @param expiresIn    access token expiration time in seconds
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {

    /**
     * Factory method to create an {@link AuthResponse} with default token type.
     *
     * @param accessToken  the JWT access token
     * @param refreshToken the JWT refresh token
     * @param expiresIn    expiration in seconds
     * @return a new AuthResponse instance
     */
    public static AuthResponse of(final String accessToken, final String refreshToken, final long expiresIn) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
