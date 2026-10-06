package com.andresyfr.franchise.domain.model;

import java.util.Objects;
import java.util.UUID;
import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;

/**
 * Product offered by a branch, with its available stock.
 *
 * <p>The record is immutable: every change returns a new instance. The name is
 * normalized (trimmed) and the stock must be zero or greater.</p>
 *
 * @param id       unique product identifier
 * @param branchId identifier of the branch that offers the product
 * @param name     normalized product name, unique within its branch
 * @param stock    available units, zero or greater
 */
public record Product(String id, String branchId, String name, int stock) {

    /**
     * Resource name used in error messages and error codes.
     */
    public static final String RESOURCE_NAME = "Product";

    /**
     * Maximum accepted length for a product name.
     */
    public static final int MAX_NAME_LENGTH = 100;

    private static final String INVALID_NAME_CODE = "INVALID_PRODUCT_NAME";
    private static final String INVALID_STOCK_CODE = "INVALID_STOCK";

    /**
     * Validates the invariants of a product.
     *
     * @throws NullPointerException           when {@code id} or {@code branchId} is null
     * @throws BusinessRuleViolationException when the name is invalid or the stock is negative
     */
    public Product {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(branchId, "branchId must not be null");
        name = normalizeName(name);
        validateStock(stock);
    }

    /**
     * Creates a product with a generated identifier for the given branch.
     *
     * @param branchId identifier of the branch that offers the product
     * @param name     proposed product name
     * @param stock    initial stock
     * @return a new valid product
     * @throws BusinessRuleViolationException when the name or the stock is invalid
     */
    public static Product create(String branchId, String name, int stock) {
        return new Product(UUID.randomUUID().toString(), branchId, name, stock);
    }

    /**
     * Returns a renamed copy that keeps identifier, branch and stock.
     *
     * @param newName proposed new name
     * @return a new product instance with the normalized name
     * @throws BusinessRuleViolationException when the new name is invalid
     */
    public Product rename(String newName) {
        return new Product(id, branchId, newName, stock);
    }

    /**
     * Returns a copy with a new stock value.
     *
     * @param newStock new available units
     * @return a new product instance with the updated stock
     * @throws BusinessRuleViolationException when the stock is negative
     */
    public Product updateStock(int newStock) {
        return new Product(id, branchId, name, newStock);
    }

    /**
     * Compares a normalized name with the current product name.
     *
     * @param otherName normalized name to compare
     * @return {@code true} when both names are equal
     */
    public boolean hasName(String otherName) {
        return name.equals(otherName);
    }

    /**
     * Indicates whether the product is offered by the given branch.
     *
     * @param otherBranchId branch identifier to compare
     * @return {@code true} when the product belongs to that branch
     */
    public boolean belongsTo(String otherBranchId) {
        return branchId.equals(otherBranchId);
    }

    private static String normalizeName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new BusinessRuleViolationException(INVALID_NAME_CODE, "Product name must not be blank");
        }
        String normalized = rawName.strip();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new BusinessRuleViolationException(INVALID_NAME_CODE,
                    "Product name must not exceed %d characters".formatted(MAX_NAME_LENGTH));
        }
        return normalized;
    }

    private static void validateStock(int stock) {
        if (stock < 0) {
            throw new BusinessRuleViolationException(INVALID_STOCK_CODE, "Stock must be zero or greater");
        }
    }
}
