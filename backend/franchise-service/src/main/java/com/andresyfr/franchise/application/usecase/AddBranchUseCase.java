package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.port.BranchRepository;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import reactor.core.publisher.Mono;

/**
 * Adds a new branch to an existing franchise.
 *
 * <p>Order of checks: name validity, franchise existence and name uniqueness
 * within the franchise. Invalid input is rejected before any database call.</p>
 */
public class AddBranchUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;

    /**
     * Creates the use case.
     *
     * @param franchiseRepository port used to verify that the franchise exists
     * @param branchRepository    port used to check uniqueness and persist the branch
     */
    public AddBranchUseCase(FranchiseRepository franchiseRepository, BranchRepository branchRepository) {
        this.franchiseRepository = franchiseRepository;
        this.branchRepository = branchRepository;
    }

    /**
     * Validates, creates and persists a branch for the given franchise.
     *
     * @param franchiseId identifier of the owning franchise
     * @param name        proposed branch name
     * @return the persisted branch
     * @throws com.andresyfr.franchise.domain.exception.BusinessRuleViolationException when the name is invalid
     * @throws ResourceNotFoundException when the franchise does not exist
     * @throws com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException when the franchise
     * already has a branch with that name
     */
    public Mono<Branch> execute(String franchiseId, String name) {
        return Mono.fromCallable(() -> Branch.create(franchiseId, name))
                .flatMap(branch -> franchiseRepository.findById(franchiseId)
                        .switchIfEmpty(Mono.error(() ->
                                new ResourceNotFoundException(Franchise.RESOURCE_NAME, franchiseId)))
                        .then(Mono.defer(() ->
                                BranchNameGuard.ensureAvailable(branchRepository, franchiseId, branch.name())))
                        .then(Mono.defer(() -> branchRepository.save(branch))));
    }
}
