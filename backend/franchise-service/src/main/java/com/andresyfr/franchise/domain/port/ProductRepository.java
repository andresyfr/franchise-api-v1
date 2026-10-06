package com.andresyfr.franchise.domain.port;

import com.andresyfr.franchise.domain.model.Product;
import reactor.core.publisher.Mono;

/**
 * Reactive output port used by product use cases to persist and retrieve products.
 *
 * <p>The contract does not expose persistence-specific types, so the domain and
 * application layers stay independent from MongoDB.</p>
 */
public interface ProductRepository {

    /**
     * Persists a product.
     *
     * @param product product to persist
     * @return the persisted product
     * @throws com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException
     * when another product of the same branch already uses the name
     */
    Mono<Product> save(Product product);

    /**
     * Finds a product by its identifier.
     *
     * @param id product identifier
     * @return the product, or an empty publisher when it does not exist
     */
    Mono<Product> findById(String id);

    /**
     * Checks whether a branch already offers a product with the given name.
     *
     * @param branchId identifier of the branch
     * @param name     normalized product name
     * @return {@code true} when the name is already used within the branch
     */
    Mono<Boolean> existsByBranchIdAndName(String branchId, String name);

    /**
     * Deletes a product by its identifier. Deleting a missing product completes normally.
     *
     * @param id product identifier
     * @return a publisher that completes when the deletion finishes
     */
    Mono<Void> deleteById(String id);
}