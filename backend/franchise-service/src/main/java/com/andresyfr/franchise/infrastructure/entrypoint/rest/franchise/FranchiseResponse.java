package com.andresyfr.franchise.infrastructure.entrypoint.rest.franchise;

import com.andresyfr.franchise.domain.model.Franchise;

/** Public franchise representation. @param id identifier @param name normalized name */
public record FranchiseResponse(String id, String name) {

        /**
     * Maps a domain aggregate to the REST representation.
     * @param franchise source aggregate
     * @return API response
     */
public static FranchiseResponse from(Franchise franchise) {
        return new FranchiseResponse(franchise.id(), franchise.name());
    }
}
