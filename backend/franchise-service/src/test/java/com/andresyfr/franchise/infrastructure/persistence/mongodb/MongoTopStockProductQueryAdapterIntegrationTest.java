package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.domain.model.TopStockProduct;

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

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Testcontainers(disabledWithoutDocker = true)
@DataMongoTest
@Import({MongoBranchAdapter.class, MongoProductAdapter.class, MongoTopStockProductQueryAdapter.class})
class MongoTopStockProductQueryAdapterIntegrationTest {

    private static final String FRANCHISE_ID = "franchise-1";
    private static final String OTHER_FRANCHISE_ID = "franchise-2";

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:8.0");

    @Autowired
    private MongoBranchAdapter branchAdapter;

    @Autowired
    private MongoProductAdapter productAdapter;

    @Autowired
    private MongoTopStockProductQueryAdapter queryAdapter;

    @Autowired
    private ReactiveMongoTemplate template;

    @BeforeEach
    void resetCollections() {
        template.dropCollection(BranchDocument.class)
                .then(template.dropCollection(ProductDocument.class))
                .block();
    }

    @Test
    void shouldReturnHighestStockPerBranchIncludingTiesAndSkippingEmptyBranches() {
        Branch centro = Branch.create(FRANCHISE_ID, "Centro");
        Branch norte = Branch.create(FRANCHISE_ID, "Norte");
        Branch vacia = Branch.create(FRANCHISE_ID, "Sin productos");
        Branch otra = Branch.create(OTHER_FRANCHISE_ID, "Otra franquicia");

        Product hamburguesa = Product.create(centro.id(), "Hamburguesa", 30);
        Product perro = Product.create(centro.id(), "Perro caliente", 30);
        Product gaseosa = Product.create(centro.id(), "Gaseosa", 10);
        Product papas = Product.create(norte.id(), "Papas", 12);
        Product ajeno = Product.create(otra.id(), "Producto ajeno", 999);

        Flux.just(centro, norte, vacia, otra).concatMap(branchAdapter::save)
                .thenMany(Flux.just(hamburguesa, perro, gaseosa, papas, ajeno).concatMap(productAdapter::save))
                .blockLast();

        StepVerifier.create(queryAdapter.findByFranchiseId(FRANCHISE_ID))
                .expectNext(new TopStockProduct(centro.id(), "Centro", hamburguesa.id(), "Hamburguesa", 30))
                .expectNext(new TopStockProduct(centro.id(), "Centro", perro.id(), "Perro caliente", 30))
                .expectNext(new TopStockProduct(norte.id(), "Norte", papas.id(), "Papas", 12))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenFranchiseHasNoBranches() {
        StepVerifier.create(queryAdapter.findByFranchiseId("franchise-without-branches"))
                .verifyComplete();
    }

    @Test
    void shouldIncludeBranchWhoseProductsHaveZeroStock() {
        Branch agotada = Branch.create(FRANCHISE_ID, "Agotada");
        Product producto = Product.create(agotada.id(), "Producto agotado", 0);

        branchAdapter.save(agotada).then(productAdapter.save(producto)).block();

        StepVerifier.create(queryAdapter.findByFranchiseId(FRANCHISE_ID))
                .expectNext(new TopStockProduct(agotada.id(), "Agotada", producto.id(), "Producto agotado", 0))
                .verifyComplete();
    }
}
