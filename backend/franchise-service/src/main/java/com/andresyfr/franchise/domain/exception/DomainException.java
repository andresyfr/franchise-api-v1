package com.andresyfr.franchise.domain.exception;

/**
 * Base type for every business error raised by the domain.
 * The {@code code} is a stable identifier exposed to API clients.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
