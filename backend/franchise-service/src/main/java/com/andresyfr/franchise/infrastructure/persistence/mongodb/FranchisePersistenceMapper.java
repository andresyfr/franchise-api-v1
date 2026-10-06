package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import com.andresyfr.franchise.domain.model.Franchise;

/**
 * Mapper utility class responsible for converting between the {@link Franchise} domain model
 * and the {@link FranchiseDocument} persistence entity.
 * <p>
 * This class is final and cannot be instantiated.
 * </p>
 */
final class FranchisePersistenceMapper {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FranchisePersistenceMapper() {
    }

    /**
     * Converts a {@link Franchise} domain object into a {@link FranchiseDocument} persistence entity.
     *
     * @param franchise the domain model to convert
     * @return a new {@link FranchiseDocument} containing the mapped data
     */
    static FranchiseDocument toDocument(Franchise franchise) {
        return new FranchiseDocument(franchise.id(), franchise.name());
    }

    /**
     * Converts a {@link FranchiseDocument} persistence entity into a {@link Franchise} domain object.
     *
     * @param document the database document to convert
     * @return a new {@link Franchise} domain object containing the mapped data
     */
    static Franchise toDomain(FranchiseDocument document) {
        return new Franchise(document.id(), document.name());
    }
}