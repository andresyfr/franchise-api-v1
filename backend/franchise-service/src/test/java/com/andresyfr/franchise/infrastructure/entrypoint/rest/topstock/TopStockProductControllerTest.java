package com.andresyfr.franchise.infrastructure.entrypoint.rest.topstock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.andresyfr.franchise.application.usecase.GetTopStockProductsUseCase;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.model.TopStockProduct;
import com.andresyfr.franchise.infrastructure.entrypoint.rest.error.GlobalErrorHandler;
import com.andresyfr.franchise.support.InMemoryFranchiseRepository;
import com.andresyfr.franchise.support.InMemoryTopStockProductQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class TopStockProductControllerTest {

    private static final String TOP_STOCK = "/api/v1/franchises/{franchiseId}/top-stock-products";

    private InMemoryFranchiseRepository franchiseRepository;
    private InMemoryTopStockProductQuery query;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        franchiseRepository = new InMemoryFranchiseRepository();
        query = new InMemoryTopStockProductQuery();

        TopStockProductController controller = new TopStockProductController(
                new GetTopStockProductsUseCase(franchiseRepository, query));

        client = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalErrorHandler(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)))
                .build();
    }

    @Test
    void shouldReturnTopStockProductsWithBranchInformation() {
        Franchise franchise = franchiseRepository.store(Franchise.create("Franquicia Colombia"));
        query.register(franchise.id(), new TopStockProduct("b1", "Centro", "p1", "Hamburguesa", 30));

        client.get().uri(TOP_STOCK, franchise.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].branchId").isEqualTo("b1")
                .jsonPath("$[0].branchName").isEqualTo("Centro")
                .jsonPath("$[0].productId").isEqualTo("p1")
                .jsonPath("$[0].productName").isEqualTo("Hamburguesa")
                .jsonPath("$[0].stock").isEqualTo(30);
    }

    @Test
    void shouldReturnEmptyArrayWhenFranchiseHasNoProducts() {
        Franchise franchise = franchiseRepository.store(Franchise.create("Franquicia Vacía"));

        client.get().uri(TOP_STOCK, franchise.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("[]");
    }

    @Test
    void shouldReturn404WhenFranchiseDoesNotExist() {
        client.get().uri(TOP_STOCK, "missing-franchise")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("RESOURCE_NOT_FOUND");
    }
}
