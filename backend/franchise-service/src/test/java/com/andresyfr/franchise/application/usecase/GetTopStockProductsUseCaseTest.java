package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.model.TopStockProduct;
import com.andresyfr.franchise.support.InMemoryFranchiseRepository;
import com.andresyfr.franchise.support.InMemoryTopStockProductQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class GetTopStockProductsUseCaseTest {

    private InMemoryFranchiseRepository franchiseRepository;
    private InMemoryTopStockProductQuery query;
    private GetTopStockProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        franchiseRepository = new InMemoryFranchiseRepository();
        query = new InMemoryTopStockProductQuery();
        useCase = new GetTopStockProductsUseCase(franchiseRepository, query);
    }

    @Test
    void shouldReturnTopStockProductsOfFranchise() {
        Franchise franchise = franchiseRepository.store(Franchise.create("Franquicia Colombia"));
        TopStockProduct centro = new TopStockProduct("b1", "Centro", "p1", "Hamburguesa", 30);
        TopStockProduct norte = new TopStockProduct("b2", "Norte", "p2", "Papas", 12);
        query.register(franchise.id(), centro);
        query.register(franchise.id(), norte);

        StepVerifier.create(useCase.execute(franchise.id()))
                .expectNext(centro, norte)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenFranchiseHasNoProducts() {
        Franchise franchise = franchiseRepository.store(Franchise.create("Franquicia Vacía"));

        StepVerifier.create(useCase.execute(franchise.id()))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        StepVerifier.create(useCase.execute("missing-franchise"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
