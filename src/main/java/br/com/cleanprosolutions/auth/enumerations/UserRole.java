package br.com.cleanprosolutions.auth.enumerations;

/**
 * Enumeration of user roles in the Clean Pro Solutions platform.
 *
 * <ul>
 *   <li>{@link #CLIENT} — End user who contracts services</li>
 *   <li>{@link #CONTRACTOR} — Professional who provides services</li>
 *   <li>{@link #ADMIN} — Platform administrator</li>
 * </ul>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public enum UserRole {

    /** End user who contracts cleaning or maintenance services. */
    CLIENT,

    /** Professional who offers services on the platform. */
    CONTRACTOR,

    /** Platform administrator with elevated privileges. */
    ADMIN
}
