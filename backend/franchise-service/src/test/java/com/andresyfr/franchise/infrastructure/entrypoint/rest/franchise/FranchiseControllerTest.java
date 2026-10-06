package com.andresyfr.franchise.infrastructure.entrypoint.rest.franchise;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.andresyfr.franchise.application.usecase.CreateFranchiseUseCase;
import com.andresyfr.franchise.application.usecase.RenameFranchiseUseCase;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.infrastructure.entrypoint.rest.error.GlobalErrorHandler;
import com.andresyfr.franchise.support.InMemoryFranchiseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class FranchiseControllerTest {

    private static final String FRANCHISES = "/api/v1/franchises";

    private InMemoryFranchiseRepository repository;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFranchiseRepository();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        FranchiseController controller = new FranchiseController(
                new CreateFranchiseUseCase(repository),
                new RenameFranchiseUseCase(repository));

        client = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalErrorHandler(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)))
                .validator(validator)
                .build();
    }

    @Test
    void createShouldReturn201WithFranchise() {
        client.post().uri(FRANCHISES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franquicia Colombia\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo("Franquicia Colombia");
    }

    @Test
    void createShouldReturn400WhenNameIsBlank() {
        client.post().uri(FRANCHISES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                .jsonPath("$.errors[0].field").isEqualTo("name");
    }

    @Test
    void createShouldReturn409WhenNameAlreadyExists() {
        repository.store(Franchise.create("Franquicia Colombia"));

        client.post().uri(FRANCHISES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franquicia Colombia\"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.code").isEqualTo("FRANCHISE_ALREADY_EXISTS");
    }

    @Test
    void renameShouldReturn200WithUpdatedFranchise() {
        Franchise existing = repository.store(Franchise.create("Original"));

        client.patch().uri(FRANCHISES + "/{id}/name", existing.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Renamed\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(existing.id())
                .jsonPath("$.name").isEqualTo("Renamed");
    }

    @Test
    void renameShouldReturn404WhenFranchiseDoesNotExist() {
        client.patch().uri(FRANCHISES + "/{id}/name", "missing-id")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Renamed\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("RESOURCE_NOT_FOUND");
    }
}
