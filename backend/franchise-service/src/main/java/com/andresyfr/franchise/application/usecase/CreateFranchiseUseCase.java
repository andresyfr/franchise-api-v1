package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import reactor.core.publisher.Mono;

/**
 * Creates a franchise after enforcing name uniqueness.
 */
public class CreateFranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public CreateFranchiseUseCase(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    /**
     * Validates, creates and persists a franchise without blocking.
     *
     * @param name proposed franchise name
     * @return the persisted franchise
     * @throws com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException when the name exists
     */
    public Mono<Franchise> execute(String name) {
        return Mono.fromCallable(() -> Franchise.create(name))
                .flatMap(franchise -> FranchiseNameGuard.ensureAvailable(franchiseRepository, franchise.name())
                        .then(Mono.defer(() -> franchiseRepository.save(franchise))));
    }
}