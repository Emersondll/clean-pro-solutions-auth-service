package br.com.cleanprosolutions.auth.exception;

/**
 * Exception thrown when a JWT token (access or refresh) is expired, malformed,
 * or has already been invalidated.
 *
 * <p>Maps to HTTP 401 Unauthorized.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public class TokenExpiredException extends RuntimeException {

    /**
     * @param message descriptive error message
     */
    public TokenExpiredException(final String message) {
        super(message);
    }
}
