package com.andresyfr.franchise.infrastructure.entrypoint.rest.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.andresyfr.franchise.application.usecase.AddProductUseCase;
import com.andresyfr.franchise.application.usecase.RemoveProductUseCase;
import com.andresyfr.franchise.application.usecase.RenameProductUseCase;
import com.andresyfr.franchise.application.usecase.UpdateProductStockUseCase;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.infrastructure.entrypoint.rest.error.GlobalErrorHandler;
import com.andresyfr.franchise.support.InMemoryBranchRepository;
import com.andresyfr.franchise.support.InMemoryProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class ProductControllerTest {

    private static final String PRODUCTS_OF_BRANCH = "/api/v1/branches/{branchId}/products";
    private static final String PRODUCT_OF_BRANCH = "/api/v1/branches/{branchId}/products/{productId}";
    private static final String PRODUCT_STOCK = "/api/v1/products/{productId}/stock";
    private static final String PRODUCT_NAME = "/api/v1/products/{productId}/name";

    private InMemoryBranchRepository branchRepository;
    private InMemoryProductRepository productRepository;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        branchRepository = new InMemoryBranchRepository();
        productRepository = new InMemoryProductRepository();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ProductController controller = new ProductController(
                new AddProductUseCase(branchRepository, productRepository),
                new RemoveProductUseCase(productRepository),
                new UpdateProductStockUseCase(productRepository),
                new RenameProductUseCase(productRepository));

        client = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalErrorHandler(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)))
                .validator(validator)
                .build();
    }

    @Test
    void addShouldReturn201WithProduct() {
        Branch branch = branchRepository.store(Branch.create("franchise-1", "Sucursal Centro"));

        client.post().uri(PRODUCTS_OF_BRANCH, branch.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Hamburguesa\",\"stock\":25}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.branchId").isEqualTo(branch.id())
                .jsonPath("$.name").isEqualTo("Hamburguesa")
                .jsonPath("$.stock").isEqualTo(25);
    }

    @Test
    void addShouldReturn400WhenStockIsMissing() {
        client.post().uri(PRODUCTS_OF_BRANCH, "any-branch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Hamburguesa\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                .jsonPath("$.errors[0].field").isEqualTo("stock");
    }

    @Test
    void addShouldReturn400WhenStockIsNegative() {
        client.post().uri(PRODUCTS_OF_BRANCH, "any-branch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Hamburguesa\",\"stock\":-1}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("stock");
    }

    @Test
    void addShouldReturn404WhenBranchDoesNotExist() {
        client.post().uri(PRODUCTS_OF_BRANCH, "missing-branch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Hamburguesa\",\"stock\":1}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void addShouldReturn409WhenNameExistsInBranch() {
        Branch branch = branchRepository.store(Branch.create("franchise-1", "Sucursal Centro"));
        productRepository.store(Product.create(branch.id(), "Hamburguesa", 1));

        client.post().uri(PRODUCTS_OF_BRANCH, branch.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Hamburguesa\",\"stock\":1}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.code").isEqualTo("PRODUCT_ALREADY_EXISTS");
    }

    @Test
    void removeShouldReturn204() {
        Product product = productRepository.store(Product.create("branch-1", "Hamburguesa", 1));

        client.delete().uri(PRODUCT_OF_BRANCH, "branch-1", product.id())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(productRepository.contains(product.id())).isFalse();
    }

    @Test
    void removeShouldReturn404WhenProductBelongsToAnotherBranch() {
        Product product = productRepository.store(Product.create("branch-1", "Hamburguesa", 1));

        client.delete().uri(PRODUCT_OF_BRANCH, "branch-2", product.id())
                .exchange()
                .expectStatus().isNotFound();

        assertThat(productRepository.contains(product.id())).isTrue();
    }

    @Test
    void updateStockShouldReturn200() {
        Product product = productRepository.store(Product.create("branch-1", "Hamburguesa", 1));

        client.patch().uri(PRODUCT_STOCK, product.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":40}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(40);
    }

    @Test
    void updateStockShouldReturn404WhenProductDoesNotExist() {
        client.patch().uri(PRODUCT_STOCK, "missing-product")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":40}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void renameShouldReturn200() {
        Product product = productRepository.store(Product.create("branch-1", "Original", 3));

        client.patch().uri(PRODUCT_NAME, product.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Renamed\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Renamed")
                .jsonPath("$.stock").isEqualTo(3);
    }
}
