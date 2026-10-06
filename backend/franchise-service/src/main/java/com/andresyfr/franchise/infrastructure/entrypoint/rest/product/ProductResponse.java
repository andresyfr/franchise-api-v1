package com.andresyfr.franchise.infrastructure.entrypoint.rest.product;

import com.andresyfr.franchise.domain.model.Product;

/**
 * Public representation of a product returned by the REST API.
 *
 * @param id       product identifier
 * @param branchId identifier of the branch that offers the product
 * @param name     normalized product name
 * @param stock    available units
 */
public record ProductResponse(String id, String branchId, String name, int stock) {

    /**
     * Maps a domain product to its API representation.
     *
     * @param product source product
     * @return API response
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.id(), product.branchId(), product.name(), product.stock());
    }
}