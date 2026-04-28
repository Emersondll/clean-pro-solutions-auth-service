package br.com.cleanprosolutions.auth.exception;

/**
 * Exception thrown when attempting to register a user whose email is already in use.
 *
 * <p>Maps to HTTP 409 Conflict.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * @param email the email that is already registered
     */
    public UserAlreadyExistsException(final String email) {
        super("User with email '" + email + "' is already registered");
    }
}
