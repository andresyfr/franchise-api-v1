package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import reactor.core.publisher.Mono;

/**
 * Shared rule: franchise names are unique.
 */
final class FranchiseNameGuard {

    private FranchiseNameGuard() {
    }

    static Mono<Void> ensureAvailable(FranchiseRepository franchiseRepository, String name) {
        return franchiseRepository.existsByName(name)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.<Void>error(new ResourceAlreadyExistsException(Franchise.RESOURCE_NAME, "name", name))
                        : Mono.<Void>empty());
    }
}