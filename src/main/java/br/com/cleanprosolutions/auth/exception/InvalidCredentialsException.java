package br.com.cleanprosolutions.auth.exception;

/**
 * Exception thrown when the provided credentials (email/password) are invalid or
 * when the account is inactive.
 *
 * <p>Maps to HTTP 401 Unauthorized.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * @param message descriptive error message (avoid exposing internal details)
     */
    public InvalidCredentialsException(final String message) {
        super(message);
    }
}
