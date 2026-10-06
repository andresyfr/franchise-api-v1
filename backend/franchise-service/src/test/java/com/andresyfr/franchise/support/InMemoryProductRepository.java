package com.andresyfr.franchise.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.domain.port.ProductRepository;

import reactor.core.publisher.Mono;

/**
 * Test double for the product output port.
 */
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<Product> save(Product product) {
        return Mono.fromCallable(() -> {
            storage.put(product.id(), product);
            return product;
        });
    }

    @Override
    public Mono<Product> findById(String id) {
        return Mono.defer(() -> Mono.justOrEmpty(storage.get(id)));
    }

    @Override
    public Mono<Boolean> existsByBranchIdAndName(String branchId, String name) {
        return Mono.fromCallable(() -> storage.values().stream()
                .anyMatch(product -> product.belongsTo(branchId) && product.hasName(name)));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> storage.remove(id));
    }

    public Product store(Product product) {
        storage.put(product.id(), product);
        return product;
    }

    public boolean contains(String id) {
        return storage.containsKey(id);
    }

    public int count() {
        return storage.size();
    }
}
