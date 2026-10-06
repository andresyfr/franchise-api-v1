package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.port.BranchRepository;
import reactor.core.publisher.Mono;

/**
 * Shared rule: branch names are unique within the same franchise.
 */
final class BranchNameGuard {

    private BranchNameGuard() {
    }

    static Mono<Void> ensureAvailable(BranchRepository branchRepository, String franchiseId, String name) {
        return branchRepository.existsByFranchiseIdAndName(franchiseId, name)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.<Void>error(new ResourceAlreadyExistsException(Branch.RESOURCE_NAME, "name", name))
                        : Mono.<Void>empty());
    }
}
