package com.andresyfr.franchise.infrastructure.entrypoint.rest.error;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;
import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

class GlobalErrorHandlerTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-10-06T10:00:00Z");

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        client = WebTestClient
                .bindToController(new FakeController())
                .controllerAdvice(new GlobalErrorHandler(Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC)))
                .validator(validator)
                .build();
    }

    @Test
    void shouldReturnNotFoundWhenResourceDoesNotExist() {
        client.get().uri("/not-found").exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("RESOURCE_NOT_FOUND")
                .jsonPath("$.detail").isEqualTo("Franchise with id '123' was not found")
                .jsonPath("$.timestamp").isEqualTo(FIXED_INSTANT.toString());
    }

    @Test
    void shouldReturnUnprocessableEntityWhenBusinessRuleIsViolated() {
        client.get().uri("/business-rule").exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.code").isEqualTo("NEGATIVE_STOCK");
    }

    @Test
    void shouldReturnFieldViolationsWhenBodyIsInvalid() {
        client.post().uri("/validated")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                .jsonPath("$.errors[0].field").isEqualTo("name");
    }

    @Test
    void shouldHideInternalDetailsOnUnexpectedErrors() {
        client.get().uri("/unexpected").exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INTERNAL_ERROR")
                .jsonPath("$.detail").isEqualTo("An unexpected error occurred");
    }

    record NameRequest(@NotBlank String name) {
    }

    @RestController
    static class FakeController {

        @GetMapping("/not-found")
        Mono<Void> notFound() {
            return Mono.error(new ResourceNotFoundException("Franchise", "123"));
        }

        @GetMapping("/business-rule")
        Mono<Void> businessRule() {
            return Mono.error(new BusinessRuleViolationException("NEGATIVE_STOCK", "Stock cannot be negative"));
        }

        @PostMapping("/validated")
        Mono<Void> validated(@Valid @RequestBody NameRequest request) {
            return Mono.empty();
        }

        @GetMapping("/unexpected")
        Mono<Void> unexpected() {
            return Mono.error(new IllegalStateException("database password leaked in message"));
        }
    }
}
