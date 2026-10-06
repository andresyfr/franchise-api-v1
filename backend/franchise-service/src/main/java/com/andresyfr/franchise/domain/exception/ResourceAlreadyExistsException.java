package com.andresyfr.franchise.domain.exception;

import java.util.Locale;

/**
 * Indicates that a unique business value is already in use.
 */
public class ResourceAlreadyExistsException extends DomainException {

    public ResourceAlreadyExistsException(String resourceName, String field, String value) {
        super(resourceName.toUpperCase(Locale.ROOT) + "_ALREADY_EXISTS",
                "%s with %s '%s' already exists".formatted(resourceName, field, value));
    }
}
