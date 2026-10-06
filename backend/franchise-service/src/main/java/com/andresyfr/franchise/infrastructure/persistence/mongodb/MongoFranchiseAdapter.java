package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * MongoDB implementation of the franchise output port.
 * Persistence exceptions are translated into domain failures.
 */
@Component
public class MongoFranchiseAdapter implements FranchiseRepository {

    private final ReactiveMongoFranchiseRepository repository;

    public MongoFranchiseAdapter(ReactiveMongoFranchiseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return repository.save(FranchisePersistenceMapper.toDocument(franchise))
                .map(FranchisePersistenceMapper::toDomain)
                .onErrorMap(DuplicateKeyException.class, error ->
                        new ResourceAlreadyExistsException(Franchise.RESOURCE_NAME, "name", franchise.name()));
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return repository.findById(id).map(FranchisePersistenceMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }
}