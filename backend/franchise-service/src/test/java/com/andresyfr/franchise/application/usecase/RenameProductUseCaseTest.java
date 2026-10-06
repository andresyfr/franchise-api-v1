package com.andresyfr.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.support.InMemoryProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class RenameProductUseCaseTest {

    private static final String BRANCH_ID = "branch-1";

    private InMemoryProductRepository repository;
    private RenameProductUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
        useCase = new RenameProductUseCase(repository);
    }

    @Test
    void shouldRenameProductKeepingStock() {
        Product product = repository.store(Product.create(BRANCH_ID, "Original", 8));

        StepVerifier.create(useCase.execute(product.id(), "Renamed"))
                .assertNext(renamed -> {
                    assertThat(renamed.id()).isEqualTo(product.id());
                    assertThat(renamed.name()).isEqualTo("Renamed");
                    assertThat(renamed.stock()).isEqualTo(8);
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenProductDoesNotExist() {
        StepVerifier.create(useCase.execute("missing-product", "Renamed"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldRejectNameUsedInSameBranch() {
        Product product = repository.store(Product.create(BRANCH_ID, "Original", 1));
        repository.store(Product.create(BRANCH_ID, "Taken", 1));

        StepVerifier.create(useCase.execute(product.id(), "Taken"))
                .expectError(ResourceAlreadyExistsException.class)
                .verify();
    }

    @Test
    void shouldReturnCurrentProductWhenNameDoesNotChange() {
        Product product = repository.store(Product.create(BRANCH_ID, "Same", 1));

        StepVerifier.create(useCase.execute(product.id(), " Same "))
                .expectNext(product)
                .verifyComplete();
    }
}
