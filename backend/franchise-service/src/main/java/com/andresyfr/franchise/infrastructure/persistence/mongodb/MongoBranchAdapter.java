package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.port.BranchRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * MongoDB implementation of the {@link BranchRepository} output port.
 *
 * <p>Persistence exceptions are translated into domain errors so use cases do
 * not depend on Spring Data.</p>
 */
@Component
public class MongoBranchAdapter implements BranchRepository {

    private final ReactiveMongoBranchRepository repository;

    /**
     * Creates the adapter.
     *
     * @param repository Spring Data repository for branch documents
     */
    public MongoBranchAdapter(ReactiveMongoBranchRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists the branch. A duplicate-key error caused by a concurrent request
     * is translated into {@link ResourceAlreadyExistsException}.
     *
     * @param branch branch to persist
     * @return the persisted branch
     */
    @Override
    public Mono<Branch> save(Branch branch) {
        return repository.save(BranchPersistenceMapper.toDocument(branch))
                .map(BranchPersistenceMapper::toDomain)
                .onErrorMap(DuplicateKeyException.class, error ->
                        new ResourceAlreadyExistsException(Branch.RESOURCE_NAME, "name", branch.name()));
    }

    @Override
    public Mono<Branch> findById(String id) {
        return repository.findById(id).map(BranchPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByFranchiseIdAndName(String franchiseId, String name) {
        return repository.existsByFranchiseIdAndName(franchiseId, name);
    }
}
