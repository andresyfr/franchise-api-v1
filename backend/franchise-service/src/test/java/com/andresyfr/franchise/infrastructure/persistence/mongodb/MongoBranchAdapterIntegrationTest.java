package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Branch;

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
@Import(MongoBranchAdapter.class)
class MongoBranchAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:8.0");

    @Autowired
    private MongoBranchAdapter adapter;

    @Autowired
    private ReactiveMongoTemplate template;

    @BeforeEach
    void resetCollection() {
        template.dropCollection(BranchDocument.class)
                .then(template.indexOps(BranchDocument.class)
                        .ensureIndex(new Index()
                                .on("franchiseId", Sort.Direction.ASC)
                                .on("name", Sort.Direction.ASC)
                                .unique()))
                .block();
    }

    @Test
    void shouldSaveAndFindBranch() {
        Branch branch = Branch.create("franchise-1", "Sucursal Centro");

        StepVerifier.create(adapter.save(branch).then(adapter.findById(branch.id())))
                .expectNext(branch)
                .verifyComplete();
    }

    @Test
    void shouldCheckNameExistenceWithinFranchise() {
        StepVerifier.create(adapter.save(Branch.create("franchise-1", "Centro"))
                        .then(adapter.existsByFranchiseIdAndName("franchise-1", "Centro")))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(adapter.existsByFranchiseIdAndName("franchise-2", "Centro"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldAllowSameNameInDifferentFranchises() {
        StepVerifier.create(adapter.save(Branch.create("franchise-1", "Centro"))
                        .then(adapter.save(Branch.create("franchise-2", "Centro"))))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldTranslateDuplicateKeyIntoDomainException() {
        StepVerifier.create(adapter.save(Branch.create("franchise-1", "Centro"))
                        .then(adapter.save(Branch.create("franchise-1", "Centro"))))
                .expectError(ResourceAlreadyExistsException.class)
                .verify();
    }
}
