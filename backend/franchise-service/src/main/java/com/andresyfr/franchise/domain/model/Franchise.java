package com.andresyfr.franchise.domain.model;

import java.util.Objects;
import java.util.UUID;
import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;

/**
 * Franchise aggregate. Immutable: every change returns a new instance.
 */
public record Franchise(String id, String name) {

    public static final String RESOURCE_NAME = "Franchise";
    public static final int MAX_NAME_LENGTH = 100;
    private static final String INVALID_NAME_CODE = "INVALID_FRANCHISE_NAME";

    public Franchise {
        Objects.requireNonNull(id, "id must not be null");
        name = normalizeName(name);
    }

    /**
     * Creates a franchise with a generated identifier and normalized name.
     *
     * @param name proposed franchise name
     * @return a new valid franchise
     * @throws com.andresyfr.franchise.domain.exception.BusinessRuleViolationException if the name is invalid
     */
    public static Franchise create(String name) {
        return new Franchise(UUID.randomUUID().toString(), name);
    }

    /**
     * Returns a renamed copy while preserving the aggregate identifier.
     *
     * @param newName proposed new name
     * @return a new franchise instance
     * @throws com.andresyfr.franchise.domain.exception.BusinessRuleViolationException if the name is invalid
     */
    public Franchise rename(String newName) {
        return new Franchise(id, newName);
    }

    /**
     * Compares a normalized candidate with the current name.
     *
     * @param otherName normalized name to compare
     * @return whether both names are equal
     */
    public boolean hasName(String otherName) {
        return name.equals(otherName);
    }

    private static String normalizeName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new BusinessRuleViolationException(INVALID_NAME_CODE, "Franchise name must not be blank");
        }
        String normalized = rawName.strip();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new BusinessRuleViolationException(INVALID_NAME_CODE,
                    "Franchise name must not exceed %d characters".formatted(MAX_NAME_LENGTH));
        }
        return normalized;
    }
}