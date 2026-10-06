package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Franchise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import reactor.test.StepVerifier;

@Testcontainers(disabledWithoutDocker = true)
@DataMongoTest
@Import(MongoFranchiseAdapter.class)
class MongoFranchiseAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:8.0");

    @Autowired
    private MongoFranchiseAdapter adapter;

    @Autowired
    private ReactiveMongoTemplate template;

    @BeforeEach
    void resetCollection() {
        template.dropCollection(FranchiseDocument.class)
                .then(template.indexOps(FranchiseDocument.class)
                        .ensureIndex(new Index().on("name", Sort.Direction.ASC).unique()))
                .block();
    }

    @Test
    void shouldSaveAndFindFranchise() {
        Franchise franchise = Franchise.create("Franquicia Colombia");

        StepVerifier.create(adapter.save(franchise).then(adapter.findById(franchise.id())))
                .expectNext(franchise)
                .verifyComplete();
    }

    @Test
    void shouldReportWhetherNameExists() {
        StepVerifier.create(adapter.save(Franchise.create("Existing"))
                        .then(adapter.existsByName("Existing")))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(adapter.existsByName("Missing"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldTranslateDuplicateKeyIntoDomainException() {
        StepVerifier.create(adapter.save(Franchise.create("Duplicated"))
                        .then(adapter.save(Franchise.create("Duplicated"))))
                .expectError(ResourceAlreadyExistsException.class)
                .verify();
    }
}
