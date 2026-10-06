package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Spring Data repository used internally by {@link MongoBranchAdapter}.
 */
public interface ReactiveMongoBranchRepository extends ReactiveMongoRepository<BranchDocument, String> {

    /**
     * Checks whether a franchise already has a branch with the given name.
     *
     * @param franchiseId identifier of the owning franchise
     * @param name        normalized branch name
     * @return {@code true} when a matching document exists
     */
    Mono<Boolean> existsByFranchiseIdAndName(String franchiseId, String name);
}
