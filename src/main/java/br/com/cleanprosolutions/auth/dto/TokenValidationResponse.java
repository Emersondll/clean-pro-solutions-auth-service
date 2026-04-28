package br.com.cleanprosolutions.auth.dto;

/**
 * Response DTO for token validation requests.
 *
 * <p>Used internally by the BFF and other services that need to validate a JWT
 * and retrieve user identity without holding the signing secret.</p>
 *
 * @param valid   whether the provided token is valid and not expired
 * @param email   the user's email extracted from the token claims (null if invalid)
 * @param role    the user's role extracted from the token claims (null if invalid)
 * @param userId  the user's ID (subject) extracted from the token (null if invalid)
 */
public record TokenValidationResponse(
        boolean valid,
        String email,
        String role,
        String userId
) {

    /**
     * Factory method for an invalid token response.
     *
     * @return response indicating an invalid token
     */
    public static TokenValidationResponse invalid() {
        return new TokenValidationResponse(false, null, null, null);
    }
}
