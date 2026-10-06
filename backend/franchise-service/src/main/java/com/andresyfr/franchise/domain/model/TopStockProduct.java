package com.andresyfr.franchise.domain.model;

import java.util.Objects;

/**
 * Read model with the product that has the highest stock in a branch.
 *
 * <p>When several products of the same branch share the highest stock, each one
 * is represented by its own instance.</p>
 *
 * @param branchId    identifier of the branch
 * @param branchName  name of the branch
 * @param productId   identifier of the product
 * @param productName name of the product
 * @param stock       highest stock found in the branch
 */
public record TopStockProduct(
        String branchId,
        String branchName,
        String productId,
        String productName,
        int stock) {

    /**
     * Validates that every identifier and name is present.
     *
     * @throws NullPointerException when a required value is null
     */
    public TopStockProduct {
        Objects.requireNonNull(branchId, "branchId must not be null");
        Objects.requireNonNull(branchName, "branchName must not be null");
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(productName, "productName must not be null");
    }
}
