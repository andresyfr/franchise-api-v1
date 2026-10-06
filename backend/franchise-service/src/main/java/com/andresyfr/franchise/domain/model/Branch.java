package com.andresyfr.franchise.domain.model;

import java.util.Objects;
import java.util.UUID;
import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;

/**
 * Branch that belongs to a franchise and offers products.
 *
 * <p>The record is immutable: every change returns a new instance. The name is
 * normalized (trimmed) on construction so that uniqueness checks compare the
 * same value that is persisted.</p>
 *
 * @param id          unique branch identifier
 * @param franchiseId identifier of the owning franchise
 * @param name        normalized branch name, unique within its franchise
 */
public record Branch(String id, String franchiseId, String name) {

    /**
     * Resource name used in error messages and error codes.
     */
    public static final String RESOURCE_NAME = "Branch";

    /**
     * Maximum accepted length for a branch name.
     */
    public static final int MAX_NAME_LENGTH = 100;

    private static final String INVALID_NAME_CODE = "INVALID_BRANCH_NAME";

    /**
     * Validates the invariants of a branch.
     *
     * @throws NullPointerException           when {@code id} or {@code franchiseId} is null
     * @throws BusinessRuleViolationException when the name is blank or too long
     */
    public Branch {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(franchiseId, "franchiseId must not be null");
        name = normalizeName(name);
    }

    /**
     * Creates a branch with a generated identifier for the given franchise.
     *
     * @param franchiseId identifier of the owning franchise
     * @param name        proposed branch name
     * @return a new valid branch
     * @throws BusinessRuleViolationException when the name is invalid
     */
    public static Branch create(String franchiseId, String name) {
        return new Branch(UUID.randomUUID().toString(), franchiseId, name);
    }

    /**
     * Returns a renamed copy that keeps the identifier and the owning franchise.
     *
     * @param newName proposed new name
     * @return a new branch instance with the normalized name
     * @throws BusinessRuleViolationException when the new name is invalid
     */
    public Branch rename(String newName) {
        return new Branch(id, franchiseId, newName);
    }

    /**
     * Compares a normalized name with the current branch name.
     *
     * @param otherName normalized name to compare
     * @return {@code true} when both names are equal
     */
    public boolean hasName(String otherName) {
        return name.equals(otherName);
    }

    private static String normalizeName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new BusinessRuleViolationException(INVALID_NAME_CODE, "Branch name must not be blank");
        }
        String normalized = rawName.strip();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new BusinessRuleViolationException(INVALID_NAME_CODE,
                    "Branch name must not exceed %d characters".formatted(MAX_NAME_LENGTH));
        }
        return normalized;
    }
}
