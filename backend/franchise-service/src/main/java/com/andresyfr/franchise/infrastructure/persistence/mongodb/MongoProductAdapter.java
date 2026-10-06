package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.domain.port.ProductRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * MongoDB implementation of the {@link ProductRepository} output port.
 *
 * <p>Persistence exceptions are translated into domain errors so use cases do
 * not depend on Spring Data.</p>
 */
@Component
public class MongoProductAdapter implements ProductRepository {

    private final ReactiveMongoProductRepository repository;

    /**
     * Creates the adapter.
     *
     * @param repository Spring Data repository for product documents
     */
    public MongoProductAdapter(ReactiveMongoProductRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists the product. A duplicate-key error caused by a concurrent request
     * is translated into {@link ResourceAlreadyExistsException}.
     *
     * @param product product to persist
     * @return the persisted product
     */
    @Override
    public Mono<Product> save(Product product) {
        return repository.save(ProductPersistenceMapper.toDocument(product))
                .map(ProductPersistenceMapper::toDomain)
                .onErrorMap(DuplicateKeyException.class, error ->
                        new ResourceAlreadyExistsException(Product.RESOURCE_NAME, "name", product.name()));
    }

    @Override
    public Mono<Product> findById(String id) {
        return repository.findById(id).map(ProductPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByBranchIdAndName(String branchId, String name) {
        return repository.existsByBranchIdAndName(branchId, name);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }
}
