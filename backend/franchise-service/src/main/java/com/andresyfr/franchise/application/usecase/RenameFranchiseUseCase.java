package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import reactor.core.publisher.Mono;

/**
 * Renames a franchise while preserving identity and name uniqueness.
 */
public class RenameFranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public RenameFranchiseUseCase(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    /**
     * Renames and persists an existing franchise.
     *
     * @param franchiseId franchise identifier
     * @param newName     proposed new name
     * @return the current or updated aggregate
     * @throws com.andresyfr.franchise.domain.exception.ResourceNotFoundException      when it does not exist
     * @throws com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException when the name is occupied
     */
    public Mono<Franchise> execute(String franchiseId, String newName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() -> new ResourceNotFoundException(Franchise.RESOURCE_NAME, franchiseId)))
                .flatMap(current -> rename(current, newName));
    }

    private Mono<Franchise> rename(Franchise current, String newName) {
        Franchise renamed = current.rename(newName);
        if (current.hasName(renamed.name())) {
            return Mono.just(current);
        }
        return FranchiseNameGuard.ensureAvailable(franchiseRepository, renamed.name())
                .then(Mono.defer(() -> franchiseRepository.save(renamed)));
    }
}