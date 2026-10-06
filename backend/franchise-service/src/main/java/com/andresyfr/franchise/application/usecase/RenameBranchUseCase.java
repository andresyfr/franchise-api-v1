package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.port.BranchRepository;
import reactor.core.publisher.Mono;

/**
 * Renames an existing branch while keeping its identifier, its franchise and
 * the uniqueness of names within that franchise.
 */
public class RenameBranchUseCase {

    private final BranchRepository branchRepository;

    /**
     * Creates the use case.
     *
     * @param branchRepository port used to read, validate and persist branches
     */
    public RenameBranchUseCase(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    /**
     * Renames a branch and persists the change.
     *
     * <p>When the normalized name does not change, the current branch is
     * returned without writing to the database.</p>
     *
     * @param branchId identifier of the branch to rename
     * @param newName  proposed new name
     * @return the current or updated branch
     * @throws ResourceNotFoundException when the branch does not exist
     * @throws com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException when another
     * branch of the same franchise already uses the name
     */
    public Mono<Branch> execute(String branchId, String newName) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(() -> new ResourceNotFoundException(Branch.RESOURCE_NAME, branchId)))
                .flatMap(current -> rename(current, newName));
    }

    private Mono<Branch> rename(Branch current, String newName) {
        Branch renamed = current.rename(newName);
        if (current.hasName(renamed.name())) {
            return Mono.just(current);
        }
        return BranchNameGuard.ensureAvailable(branchRepository, current.franchiseId(), renamed.name())
                .then(Mono.defer(() -> branchRepository.save(renamed)));
    }
}
