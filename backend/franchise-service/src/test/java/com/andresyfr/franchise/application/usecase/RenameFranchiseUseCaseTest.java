package com.andresyfr.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.support.InMemoryFranchiseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class RenameFranchiseUseCaseTest {

    private InMemoryFranchiseRepository repository;
    private RenameFranchiseUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFranchiseRepository();
        useCase = new RenameFranchiseUseCase(repository);
    }

    @Test
    void shouldRenameFranchise() {
        Franchise existing = repository.store(Franchise.create("Original"));

        StepVerifier.create(useCase.execute(existing.id(), "Renamed"))
                .assertNext(franchise -> {
                    assertThat(franchise.id()).isEqualTo(existing.id());
                    assertThat(franchise.name()).isEqualTo("Renamed");
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        StepVerifier.create(useCase.execute("missing-id", "Renamed"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldRejectNameUsedByAnotherFranchise() {
        Franchise existing = repository.store(Franchise.create("Original"));
        repository.store(Franchise.create("Taken"));

        StepVerifier.create(useCase.execute(existing.id(), "Taken"))
                .expectError(ResourceAlreadyExistsException.class)
                .verify();
    }

    @Test
    void shouldReturnCurrentFranchiseWhenNameDoesNotChange() {
        Franchise existing = repository.store(Franchise.create("Same"));

        StepVerifier.create(useCase.execute(existing.id(), "  Same "))
                .expectNext(existing)
                .verifyComplete();
    }
}
