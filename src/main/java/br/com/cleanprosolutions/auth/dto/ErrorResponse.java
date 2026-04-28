package br.com.cleanprosolutions.auth.dto;

import java.time.Instant;

/**
 * Standard error response DTO for all exception handlers in the auth-service.
 *
 * <p>All error responses follow this consistent structure to allow the BFF
 * and clients to handle errors uniformly.</p>
 *
 * @param timestamp     when the error occurred (UTC)
 * @param status        HTTP status code
 * @param message       human-readable error description
 * @param correlationId request correlation ID for distributed tracing (may be null)
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String message,
        String correlationId
) {

    /**
     * Factory method to create an {@link ErrorResponse} with current timestamp.
     *
     * @param status        HTTP status code
     * @param message       error message
     * @param correlationId correlation ID for tracing
     * @return a new ErrorResponse instance
     */
    public static ErrorResponse of(final int status, final String message, final String correlationId) {
        return new ErrorResponse(Instant.now(), status, message, correlationId);
    }
}
