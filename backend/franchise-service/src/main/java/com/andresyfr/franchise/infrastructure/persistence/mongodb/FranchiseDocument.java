package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB representation of a franchise.
 * The unique name index is the concurrency-safe uniqueness guard.
 * @param id aggregate identifier
 * @param name normalized franchise name
 */
@Document(collection = "franchises")
public record FranchiseDocument(
        @Id String id,
        @Indexed(unique = true) String name) {
}