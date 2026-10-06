package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.domain.port.ProductRepository;

import reactor.core.publisher.Mono;

/**
 * Shared rule: product names are unique within the same branch.
 */
final class ProductNameGuard {

    private ProductNameGuard() {
    }

    static Mono<Void> ensureAvailable(ProductRepository productRepository, String branchId, String name) {
        return productRepository.existsByBranchIdAndName(branchId, name)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.<Void>error(new ResourceAlreadyExistsException(Product.RESOURCE_NAME, "name", name))
                        : Mono.<Void>empty());
    }
}
