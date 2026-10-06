package com.andresyfr.franchise.infrastructure.entrypoint.rest.branch;

import com.andresyfr.franchise.domain.model.Branch;

/**
 * Public representation of a branch returned by the REST API.
 *
 * @param id          branch identifier
 * @param franchiseId identifier of the owning franchise
 * @param name        normalized branch name
 */
public record BranchResponse(String id, String franchiseId, String name) {

    /**
     * Maps a domain branch to its API representation.
     *
     * @param branch source branch
     * @return API response
     */
    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.id(), branch.franchiseId(), branch.name());
    }
}
