package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Spring Data repository used internally by the MongoDB adapter.
 */
public interface ReactiveMongoFranchiseRepository extends ReactiveMongoRepository<FranchiseDocument, String> {
    Mono<Boolean> existsByName(String name);
}