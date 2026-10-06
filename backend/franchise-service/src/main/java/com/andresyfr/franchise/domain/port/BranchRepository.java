package com.andresyfr.franchise.domain.port;

import com.andresyfr.franchise.domain.model.Branch;
import reactor.core.publisher.Mono;

/**
 * Reactive output port used by branch use cases to persist and retrieve branches.
 *
 * <p>The contract does not expose persistence-specific types, so the domain and
 * application layers stay independent from MongoDB.</p>
 */
public interface BranchRepository {

    /**
     * Persists a branch.
     *
     * @param branch branch to persist
     * @return the persisted branch
     * @throws com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException
     *         when another branch of the same franchise already uses the name
     */
    Mono<Branch> save(Branch branch);

    /**
     * Finds a branch by its identifier.
     *
     * @param id branch identifier
     * @return the branch, or an empty publisher when it does not exist
     */
    Mono<Branch> findById(String id);

    /**
     * Checks whether a franchise already has a branch with the given name.
     *
     * @param franchiseId identifier of the owning franchise
     * @param name        normalized branch name
     * @return {@code true} when the name is already used within the franchise
     */
    Mono<Boolean> existsByFranchiseIdAndName(String franchiseId, String name);
}