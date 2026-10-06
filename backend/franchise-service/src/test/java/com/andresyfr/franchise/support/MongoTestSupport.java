package com.andresyfr.franchise.support;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Query;

import reactor.core.publisher.Flux;

/**
 * Prepares MongoDB collections for integration tests.
 *
 * <p>Documents are removed but collections are not dropped, and indexes are
 * created from the entity annotations. Because the definition (name and options)
 * matches the one Spring Data creates automatically, the operation is idempotent
 * and cannot conflict with the asynchronous index creation of the reactive stack.</p>
 */
public final class MongoTestSupport {

    private MongoTestSupport() {
    }

    /**
     * Removes all documents and ensures the annotated indexes of each document type.
     *
     * @param template      reactive template of the test context
     * @param documentTypes MongoDB document classes to reset
     */
    public static void reset(ReactiveMongoTemplate template, Class<?>... documentTypes) {
        IndexResolver resolver = IndexResolver.create(template.getConverter().getMappingContext());

        Flux.fromArray(documentTypes)
                .concatMap(type -> {
                    ReactiveIndexOperations indexOps = template.indexOps(type);
                    return template.remove(new Query(), type)
                            .thenMany(Flux.fromIterable(resolver.resolveIndexFor(type)))
                            .concatMap(indexOps::createIndex);
                })
                .then()
                .block();
    }
}
