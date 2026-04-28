package br.com.cleanprosolutions.auth.repository;

import br.com.cleanprosolutions.auth.document.UserCredential;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link UserCredential} persistence operations.
 *
 * <p>Extends {@link MongoRepository} providing standard CRUD operations
 * plus custom finders for authentication flows.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Repository
public interface UserCredentialRepository extends MongoRepository<UserCredential, String> {

    /**
     * Finds a user credential by email address.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the credential if found
     */
    Optional<UserCredential> findByEmail(String email);

    /**
     * Checks whether an email is already registered.
     *
     * @param email the email to check
     * @return {@code true} if the email is already in use
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user credential by its current refresh token.
     *
     * <p>Used during token refresh to locate the user and rotate the token.</p>
     *
     * @param refreshToken the refresh token to search for
     * @return an {@link Optional} containing the credential if found
     */
    Optional<UserCredential> findByRefreshToken(String refreshToken);
}
