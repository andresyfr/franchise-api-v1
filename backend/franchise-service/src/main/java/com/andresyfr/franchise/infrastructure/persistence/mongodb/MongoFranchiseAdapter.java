package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * MongoDB implementation adapter for the {@link FranchiseRepository} interface.
 * <p>
 * This class acts as a bridge between the domain layer repository contract
 * and the reactive MongoDB driver, handling persistence operations and mapping data.
 * </p>
 */
@Component
public class MongoFranchiseAdapter implements FranchiseRepository {

    /**
     * The reactive Spring Data MongoDB repository used for database interactions.
     */
    private final ReactiveMongoFranchiseRepository repository;

    /**
     * Constructs a new {@code MongoFranchiseAdapter} with the required reactive repository.
     *
     * @param repository the reactive MongoDB repository to inject
     */
    public MongoFranchiseAdapter(ReactiveMongoFranchiseRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves a franchise domain entity into the MongoDB database.
     * <p>
     * It maps the domain model to a database document before saving, and maps it back to
     * a domain object upon successful completion. If a unique key conflict occurs, it translates
     * the error into a domain-specific exception.
     * </p>
     *
     * @param franchise the franchise domain entity to persist
     * @return a {@link Mono} emitting the persisted franchise domain object
     * @throws ResourceAlreadyExistsException if a franchise with the same unique criteria already exists
     */
    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return repository.save(FranchisePersistenceMapper.toDocument(franchise))
                .map(FranchisePersistenceMapper::toDomain)
                .onErrorMap(DuplicateKeyException.class, error ->
                        new ResourceAlreadyExistsException(Franchise.RESOURCE_NAME, "name", franchise.name()));
    }

    /**
     * Retrieves a franchise by its unique identifier.
     *
     * @param id the unique identifier of the franchise
     * @return a {@link Mono} emitting the matching franchise, or an empty Mono if no match is found
     */
    @Override
    public Mono<Franchise> findById(String id) {
        return repository.findById(id).map(FranchisePersistenceMapper::toDomain);
    }

    /**
     * Checks if a franchise already exists in the database with the specified name.
     *
     * @param name the name of the franchise to verify
     * @return a {@link Mono} emitting {@code true} if the name exists, or {@code false} otherwise
     */
    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }
}