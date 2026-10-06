package com.andresyfr.franchise.infrastructure.entrypoint.rest.topstock;

import com.andresyfr.franchise.domain.model.TopStockProduct;

/**
 * Public representation of the highest-stock product of a branch.
 *
 * @param branchId    identifier of the branch
 * @param branchName  name of the branch
 * @param productId   identifier of the product
 * @param productName name of the product
 * @param stock       highest stock in the branch
 */
public record TopStockProductResponse(
        String branchId,
        String branchName,
        String productId,
        String productName,
        int stock) {

    /**
     * Maps the domain read model to its API representation.
     *
     * @param product source read model
     * @return API response
     */
    public static TopStockProductResponse from(TopStockProduct product) {
        return new TopStockProductResponse(
                product.branchId(),
                product.branchName(),
                product.productId(),
                product.productName(),
                product.stock());
    }
}