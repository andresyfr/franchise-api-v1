package com.andresyfr.franchise.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.andresyfr.franchise.domain.model.TopStockProduct;
import com.andresyfr.franchise.domain.port.TopStockProductQuery;

import reactor.core.publisher.Flux;

/**
 * Test double for the highest-stock read port.
 */
public class InMemoryTopStockProductQuery implements TopStockProductQuery {

    private final Map<String, List<TopStockProduct>> resultsByFranchise = new ConcurrentHashMap<>();

    @Override
    public Flux<TopStockProduct> findByFranchiseId(String franchiseId) {
        return Flux.defer(() -> Flux.fromIterable(resultsByFranchise.getOrDefault(franchiseId, List.of())));
    }

    public void register(String franchiseId, TopStockProduct product) {
        resultsByFranchise.computeIfAbsent(franchiseId, key -> new ArrayList<>()).add(product);
    }
}
