package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.support.MongoTestSupport;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import reactor.test.StepVerifier;

@Testcontainers(disabledWithoutDocker = true)
@DataMongoTest
@Import(MongoProductAdapter.class)
class MongoProductAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:8.0");

    @Autowired
    private MongoProductAdapter adapter;

    @Autowired
    private ReactiveMongoTemplate template;

    @BeforeEach
    void resetCollections() {
        MongoTestSupport.reset(template, ProductDocument.class);
    }

    @Test
    void shouldSaveAndFindProduct() {
        Product product = Product.create("branch-1", "Hamburguesa", 12);

        StepVerifier.create(adapter.save(product).then(adapter.findById(product.id())))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void shouldPersistStockChanges() {
        Product product = Product.create("branch-1", "Hamburguesa", 12);

        StepVerifier.create(adapter.save(product)
                        .then(adapter.save(product.updateStock(30)))
                        .then(adapter.findById(product.id())))
                .expectNextMatches(found -> found.stock() == 30)
                .verifyComplete();
    }

    @Test
    void shouldDeleteProduct() {
        Product product = Product.create("branch-1", "Hamburguesa", 12);

        StepVerifier.create(adapter.save(product)
                        .then(adapter.deleteById(product.id()))
                        .then(adapter.findById(product.id())))
                .verifyComplete();
    }

    @Test
    void shouldTranslateDuplicateKeyIntoDomainException() {
        StepVerifier.create(adapter.save(Product.create("branch-1", "Hamburguesa", 1))
                        .then(adapter.save(Product.create("branch-1", "Hamburguesa", 2))))
                .expectError(ResourceAlreadyExistsException.class)
                .verify();
    }

    @Test
    void shouldAllowSameNameInDifferentBranches() {
        StepVerifier.create(adapter.save(Product.create("branch-1", "Hamburguesa", 1))
                        .then(adapter.save(Product.create("branch-2", "Hamburguesa", 2))))
                .expectNextCount(1)
                .verifyComplete();
    }
}
