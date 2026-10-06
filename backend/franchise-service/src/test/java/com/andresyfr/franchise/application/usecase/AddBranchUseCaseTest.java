package com.andresyfr.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;
import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.support.InMemoryBranchRepository;
import com.andresyfr.franchise.support.InMemoryFranchiseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class AddBranchUseCaseTest {

    private InMemoryFranchiseRepository franchiseRepository;
    private InMemoryBranchRepository branchRepository;
    private AddBranchUseCase useCase;
    private Franchise franchise;

    @BeforeEach
    void setUp() {
        franchiseRepository = new InMemoryFranchiseRepository();
        branchRepository = new InMemoryBranchRepository();
        useCase = new AddBranchUseCase(franchiseRepository, branchRepository);
        franchise = franchiseRepository.store(Franchise.create("Franquicia Colombia"));
    }

    @Test
    void shouldAddBranchToExistingFranchise() {
        StepVerifier.create(useCase.execute(franchise.id(), "Sucursal Centro"))
                .assertNext(branch -> {
                    assertThat(branch.id()).isNotBlank();
                    assertThat(branch.franchiseId()).isEqualTo(franchise.id());
                    assertThat(branch.name()).isEqualTo("Sucursal Centro");
                })
                .verifyComplete();

        assertThat(branchRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        StepVerifier.create(useCase.execute("missing-franchise", "Sucursal Centro"))
                .expectError(ResourceNotFoundException.class)
                .verify();

        assertThat(branchRepository.count()).isZero();
    }

    @Test
    void shouldRejectDuplicatedNameWithinSameFranchise() {
        branchRepository.store(Branch.create(franchise.id(), "Sucursal Centro"));

        StepVerifier.create(useCase.execute(franchise.id(), " Sucursal Centro "))
                .expectError(ResourceAlreadyExistsException.class)
                .verify();

        assertThat(branchRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldAllowSameNameInDifferentFranchises() {
        Franchise other = franchiseRepository.store(Franchise.create("Franquicia Perú"));
        branchRepository.store(Branch.create(other.id(), "Sucursal Centro"));

        StepVerifier.create(useCase.execute(franchise.id(), "Sucursal Centro"))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(branchRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldRejectInvalidNameBeforeQueryingRepositories() {
        StepVerifier.create(useCase.execute("missing-franchise", "  "))
                .expectError(BusinessRuleViolationException.class)
                .verify();
    }
}
