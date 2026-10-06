package com.andresyfr.franchise.infrastructure.entrypoint.rest.error;

/**
 * Invalid request field included in an RFC 9457 response.
 * @param field rejected field
 * @param message validation message
 */
public record FieldViolation(String field, String message) {
}
