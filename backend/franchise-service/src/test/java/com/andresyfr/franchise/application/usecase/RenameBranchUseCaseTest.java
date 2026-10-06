package com.andresyfr.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.support.InMemoryBranchRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class RenameBranchUseCaseTest {

    private static final String FRANCHISE_ID = "franchise-1";

    private InMemoryBranchRepository repository;
    private RenameBranchUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBranchRepository();
        useCase = new RenameBranchUseCase(repository);
    }

    @Test
    void shouldRenameBranch() {
        Branch existing = repository.store(Branch.create(FRANCHISE_ID, "Original"));

        StepVerifier.create(useCase.execute(existing.id(), "Renamed"))
                .assertNext(branch -> {
                    assertThat(branch.id()).isEqualTo(existing.id());
                    assertThat(branch.franchiseId()).isEqualTo(FRANCHISE_ID);
                    assertThat(branch.name()).isEqualTo("Renamed");
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenBranchDoesNotExist() {
        StepVerifier.create(useCase.execute("missing-branch", "Renamed"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldRejectNameUsedBySiblingBranch() {
        Branch existing = repository.store(Branch.create(FRANCHISE_ID, "Original"));
        repository.store(Branch.create(FRANCHISE_ID, "Taken"));

        StepVerifier.create(useCase.execute(existing.id(), "Taken"))
                .expectError(ResourceAlreadyExistsException.class)
                .verify();
    }

    @Test
    void shouldAllowNameUsedInAnotherFranchise() {
        Branch existing = repository.store(Branch.create(FRANCHISE_ID, "Original"));
        repository.store(Branch.create("other-franchise", "Taken"));

        StepVerifier.create(useCase.execute(existing.id(), "Taken"))
                .assertNext(branch -> assertThat(branch.name()).isEqualTo("Taken"))
                .verifyComplete();
    }

    @Test
    void shouldReturnCurrentBranchWhenNameDoesNotChange() {
        Branch existing = repository.store(Branch.create(FRANCHISE_ID, "Same"));

        StepVerifier.create(useCase.execute(existing.id(), "  Same "))
                .expectNext(existing)
                .verifyComplete();
    }
}
