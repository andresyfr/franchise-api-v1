package com.andresyfr.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;
import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.support.InMemoryProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class UpdateProductStockUseCaseTest {

    private InMemoryProductRepository repository;
    private UpdateProductStockUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
        useCase = new UpdateProductStockUseCase(repository);
    }

    @Test
    void shouldUpdateStock() {
        Product product = repository.store(Product.create("branch-1", "Hamburguesa", 10));

        StepVerifier.create(useCase.execute(product.id(), 42))
                .assertNext(updated -> {
                    assertThat(updated.id()).isEqualTo(product.id());
                    assertThat(updated.stock()).isEqualTo(42);
                })
                .verifyComplete();
    }

    @Test
    void shouldAllowStockZero() {
        Product product = repository.store(Product.create("branch-1", "Hamburguesa", 10));

        StepVerifier.create(useCase.execute(product.id(), 0))
                .assertNext(updated -> assertThat(updated.stock()).isZero())
                .verifyComplete();
    }

    @Test
    void shouldFailWhenProductDoesNotExist() {
        StepVerifier.create(useCase.execute("missing-product", 5))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldRejectNegativeStock() {
        Product product = repository.store(Product.create("branch-1", "Hamburguesa", 10));

        StepVerifier.create(useCase.execute(product.id(), -3))
                .expectError(BusinessRuleViolationException.class)
                .verify();
    }

    @Test
    void shouldReturnCurrentProductWhenStockDoesNotChange() {
        Product product = repository.store(Product.create("branch-1", "Hamburguesa", 10));

        StepVerifier.create(useCase.execute(product.id(), 10))
                .expectNext(product)
                .verifyComplete();
    }
}
