package com.andresyfr.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.support.InMemoryProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class RemoveProductUseCaseTest {

    private static final String BRANCH_ID = "branch-1";

    private InMemoryProductRepository repository;
    private RemoveProductUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
        useCase = new RemoveProductUseCase(repository);
    }

    @Test
    void shouldRemoveProductOfBranch() {
        Product product = repository.store(Product.create(BRANCH_ID, "Hamburguesa", 3));

        StepVerifier.create(useCase.execute(BRANCH_ID, product.id()))
                .verifyComplete();

        assertThat(repository.contains(product.id())).isFalse();
    }

    @Test
    void shouldFailWhenProductDoesNotExist() {
        StepVerifier.create(useCase.execute(BRANCH_ID, "missing-product"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldNotRemoveProductThatBelongsToAnotherBranch() {
        Product product = repository.store(Product.create("other-branch", "Hamburguesa", 3));

        StepVerifier.create(useCase.execute(BRANCH_ID, product.id()))
                .expectError(ResourceNotFoundException.class)
                .verify();

        assertThat(repository.contains(product.id())).isTrue();
    }
}
