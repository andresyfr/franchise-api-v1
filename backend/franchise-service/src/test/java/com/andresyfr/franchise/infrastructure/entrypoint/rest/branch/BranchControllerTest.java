package com.andresyfr.franchise.infrastructure.entrypoint.rest.branch;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.andresyfr.franchise.application.usecase.AddBranchUseCase;
import com.andresyfr.franchise.application.usecase.RenameBranchUseCase;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.infrastructure.entrypoint.rest.error.GlobalErrorHandler;
import com.andresyfr.franchise.support.InMemoryBranchRepository;
import com.andresyfr.franchise.support.InMemoryFranchiseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class BranchControllerTest {

    private static final String BRANCHES_OF_FRANCHISE = "/api/v1/franchises/{franchiseId}/branches";
    private static final String BRANCH_NAME = "/api/v1/branches/{branchId}/name";

    private InMemoryFranchiseRepository franchiseRepository;
    private InMemoryBranchRepository branchRepository;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        franchiseRepository = new InMemoryFranchiseRepository();
        branchRepository = new InMemoryBranchRepository();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        BranchController controller = new BranchController(
                new AddBranchUseCase(franchiseRepository, branchRepository),
                new RenameBranchUseCase(branchRepository));

        client = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalErrorHandler(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)))
                .validator(validator)
                .build();
    }

    @Test
    void addShouldReturn201WithBranch() {
        Franchise franchise = franchiseRepository.store(Franchise.create("Franquicia Colombia"));

        client.post().uri(BRANCHES_OF_FRANCHISE, franchise.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Sucursal Centro\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.franchiseId").isEqualTo(franchise.id())
                .jsonPath("$.name").isEqualTo("Sucursal Centro");
    }

    @Test
    void addShouldReturn404WhenFranchiseDoesNotExist() {
        client.post().uri(BRANCHES_OF_FRANCHISE, "missing-franchise")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Sucursal Centro\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void addShouldReturn409WhenNameExistsInFranchise() {
        Franchise franchise = franchiseRepository.store(Franchise.create("Franquicia Colombia"));
        branchRepository.store(Branch.create(franchise.id(), "Sucursal Centro"));

        client.post().uri(BRANCHES_OF_FRANCHISE, franchise.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Sucursal Centro\"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.code").isEqualTo("BRANCH_ALREADY_EXISTS");
    }

    @Test
    void addShouldReturn400WhenNameIsBlank() {
        client.post().uri(BRANCHES_OF_FRANCHISE, "any-franchise")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                .jsonPath("$.errors[0].field").isEqualTo("name");
    }

    @Test
    void renameShouldReturn200WithUpdatedBranch() {
        Branch existing = branchRepository.store(Branch.create("franchise-1", "Original"));

        client.patch().uri(BRANCH_NAME, existing.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Renamed\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(existing.id())
                .jsonPath("$.franchiseId").isEqualTo("franchise-1")
                .jsonPath("$.name").isEqualTo("Renamed");
    }

    @Test
    void renameShouldReturn404WhenBranchDoesNotExist() {
        client.patch().uri(BRANCH_NAME, "missing-branch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Renamed\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("RESOURCE_NOT_FOUND");
    }
}
