package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.model.Branch;

/**
 * Converts between the branch domain model and its MongoDB document.
 */
final class BranchPersistenceMapper {

    private BranchPersistenceMapper() {
    }

    static BranchDocument toDocument(Branch branch) {
        return new BranchDocument(branch.id(), branch.franchiseId(), branch.name());
    }

    static Branch toDomain(BranchDocument document) {
        return new Branch(document.id(), document.franchiseId(), document.name());
    }
}