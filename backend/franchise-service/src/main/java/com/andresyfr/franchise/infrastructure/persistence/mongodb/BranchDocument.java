package com.andresyfr.franchise.infrastructure.persistence.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB representation of a branch.
 *
 * <p>The unique compound index on {@code franchiseId} and {@code name} enforces
 * name uniqueness per franchise, even under concurrent requests. Because
 * {@code franchiseId} is the index prefix, it also serves queries that list the
 * branches of a franchise.</p>
 *
 * @param id          branch identifier
 * @param franchiseId identifier of the owning franchise
 * @param name        normalized branch name
 */
@Document(collection = "branches")
@CompoundIndex(name = "franchise_branch_name_uq", def = "{'franchiseId': 1, 'name': 1}", unique = true)
public record BranchDocument(
        @Id String id,
        String franchiseId,
        String name) {
}
