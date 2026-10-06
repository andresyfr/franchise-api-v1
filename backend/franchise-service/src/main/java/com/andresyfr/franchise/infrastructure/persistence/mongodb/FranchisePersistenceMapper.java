package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.model.Franchise;

final class FranchisePersistenceMapper {

    private FranchisePersistenceMapper() {
    }

    static FranchiseDocument toDocument(Franchise franchise) {
        return new FranchiseDocument(franchise.id(), franchise.name());
    }

    static Franchise toDomain(FranchiseDocument document) {
        return new Franchise(document.id(), document.name());
    }
}