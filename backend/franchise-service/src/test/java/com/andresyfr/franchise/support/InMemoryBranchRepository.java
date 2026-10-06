package com.andresyfr.franchise.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.port.BranchRepository;

import reactor.core.publisher.Mono;

/**
 * Test double for the branch output port.
 */
public class InMemoryBranchRepository implements BranchRepository {

    private final Map<String, Branch> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<Branch> save(Branch branch) {
        return Mono.fromCallable(() -> {
            storage.put(branch.id(), branch);
            return branch;
        });
    }

    @Override
    public Mono<Branch> findById(String id) {
        return Mono.defer(() -> Mono.justOrEmpty(storage.get(id)));
    }

    @Override
    public Mono<Boolean> existsByFranchiseIdAndName(String franchiseId, String name) {
        return Mono.fromCallable(() -> storage.values().stream()
                .anyMatch(branch -> branch.franchiseId().equals(franchiseId) && branch.hasName(name)));
    }

    public Branch store(Branch branch) {
        storage.put(branch.id(), branch);
        return branch;
    }

    public int count() {
        return storage.size();
    }
}
