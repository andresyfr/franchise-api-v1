package com.andresyfr.franchise.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.port.FranchiseRepository;

import reactor.core.publisher.Mono;

/**
 * Test double for the franchise output port.
 */
public class InMemoryFranchiseRepository implements FranchiseRepository {

    private final Map<String, Franchise> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return Mono.fromCallable(() -> {
            storage.put(franchise.id(), franchise);
            return franchise;
        });
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return Mono.defer(() -> Mono.justOrEmpty(storage.get(id)));
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return Mono.fromCallable(() -> storage.values().stream().anyMatch(franchise -> franchise.hasName(name)));
    }

    public Franchise store(Franchise franchise) {
        storage.put(franchise.id(), franchise);
        return franchise;
    }

    public int count() {
        return storage.size();
    }
}
