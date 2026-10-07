package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.domain.port.ProductRepository;
import reactor.core.publisher.Mono;

/**
 * Removes a product from a branch.
 *
 * <p>A product that exists but belongs to another branch is reported as not
 * found, so a caller cannot delete products through an unrelated branch.</p>
 */
public class RemoveProductUseCase {

    private final ProductRepository productRepository;

    /**
     * Creates the use case.
     *
     * @param productRepository port used to read and delete products
     */
    public RemoveProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Deletes a product that belongs to the given branch.
     * It is suggested for future improvements to use logical deletion in order to update the record
     * and maintain the information for auditing and others
     *
     * @param branchId  identifier of the branch that must own the product
     * @param productId identifier of the product to delete
     * @return a publisher that completes when the product has been deleted
     * @throws ResourceNotFoundException when the product does not exist in that branch
     */
    public Mono<Void> execute(String branchId, String productId) {
        return productRepository.findById(productId)
                .filter(product -> product.belongsTo(branchId))
                .switchIfEmpty(Mono.error(() -> new ResourceNotFoundException(Product.RESOURCE_NAME, productId)))
                .flatMap(product -> productRepository.deleteById(product.id()));
    }
}
