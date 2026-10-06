package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Spring Data repository used internally by {@link MongoProductAdapter}.
 */
public interface ReactiveMongoProductRepository extends ReactiveMongoRepository<ProductDocument, String> {

    /**
     * Checks whether a branch already offers a product with the given name.
     *
     * @param branchId identifier of the branch
     * @param name     normalized product name
     * @return {@code true} when a matching document exists
     */
    Mono<Boolean> existsByBranchIdAndName(String branchId, String name);
}