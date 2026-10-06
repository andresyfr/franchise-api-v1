package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.domain.port.ProductRepository;
import reactor.core.publisher.Mono;

/**
 * Renames an existing product while keeping its identifier, branch, stock and
 * the uniqueness of names within that branch.
 */
public class RenameProductUseCase {

    private final ProductRepository productRepository;

    /**
     * Creates the use case.
     *
     * @param productRepository port used to read, validate and persist products
     */
    public RenameProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Renames a product and persists the change.
     *
     * <p>When the normalized name does not change, the current product is
     * returned without writing to the database.</p>
     *
     * @param productId identifier of the product
     * @param newName   proposed new name
     * @return the current or updated product
     * @throws ResourceNotFoundException
     * when the product does not exist
     * @throws com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException
     * when another product of the same branch already uses the name
     */
    public Mono<Product> execute(String productId, String newName) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(() -> new ResourceNotFoundException(Product.RESOURCE_NAME, productId)))
                .flatMap(current -> rename(current, newName));
    }

    private Mono<Product> rename(Product current, String newName) {
        Product renamed = current.rename(newName);
        if (current.hasName(renamed.name())) {
            return Mono.just(current);
        }
        return ProductNameGuard.ensureAvailable(productRepository, current.branchId(), renamed.name())
                .then(Mono.defer(() -> productRepository.save(renamed)));
    }
}
