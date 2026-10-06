package com.andresyfr.franchise.domain.port;

import com.andresyfr.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

/**
 * Reactive output port used by franchise use cases.
 * It does not expose persistence-specific types.
 */
public interface FranchiseRepository {

    /**
     * Persists a franchise.
     *
     * @param franchise aggregate to persist
     * @return the persisted aggregate
     */
    Mono<Franchise> save(Franchise franchise);

    /**
     * Finds a franchise by identifier.
     *
     * @param id franchise identifier
     * @return the franchise, or an empty publisher when absent
     */
    Mono<Franchise> findById(String id);

    /**
     * Checks the unique-name constraint.
     *
     * @param name normalized franchise name
     * @return whether the name is already present
     */
    Mono<Boolean> existsByName(String name);
}
