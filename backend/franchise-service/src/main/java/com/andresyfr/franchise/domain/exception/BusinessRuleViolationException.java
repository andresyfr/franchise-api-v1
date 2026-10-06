package com.andresyfr.franchise.domain.exception;

/**
 * Indicates that valid input violates a domain invariant.
 */
public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String code, String message) {
        super(code, message);
    }
}
