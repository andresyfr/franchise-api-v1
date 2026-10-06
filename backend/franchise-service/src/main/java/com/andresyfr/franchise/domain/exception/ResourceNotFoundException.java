package com.andresyfr.franchise.domain.exception;

public class ResourceNotFoundException extends DomainException {

    private static final String CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(CODE, "%s with id '%s' was not found".formatted(resourceName, resourceId));
    }
}
