package com.andresyfr.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;
import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.support.InMemoryFranchiseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class CreateFranchiseUseCaseTest {

    private InMemoryFranchiseRepository repository;
    private CreateFranchiseUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFranchiseRepository();
        useCase = new CreateFranchiseUseCase(repository);
    }

    @Test
    void shouldCreateFranchise() {
        StepVerifier.create(useCase.execute("Franquicia Colombia"))
                .assertNext(franchise -> {
                    assertThat(franchise.id()).isNotBlank();
                    assertThat(franchise.name()).isEqualTo("Franquicia Colombia");
                })
                .verifyComplete();

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectDuplicatedNameIgnoringSurroundingSpaces() {
        repository.store(Franchise.create("Franquicia Colombia"));

        StepVerifier.create(useCase.execute("  Franquicia Colombia "))
                .expectError(ResourceAlreadyExistsException.class)
                .verify();

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidNameWithoutTouchingRepository() {
        StepVerifier.create(useCase.execute("  "))
                .expectError(BusinessRuleViolationException.class)
                .verify();

        assertThat(repository.count()).isZero();
    }
}
