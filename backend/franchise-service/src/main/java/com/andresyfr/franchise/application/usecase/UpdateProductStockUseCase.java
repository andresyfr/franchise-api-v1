package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.domain.port.ProductRepository;
import reactor.core.publisher.Mono;

/**
 * Replaces the stock of an existing product.
 */
public class UpdateProductStockUseCase {

    private final ProductRepository productRepository;

    /**
     * Creates the use case.
     *
     * @param productRepository port used to read and persist products
     */
    public UpdateProductStockUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Sets the stock of a product and persists the change.
     *
     * <p>When the stock does not change, the current product is returned
     * without writing to the database.</p>
     *
     * @param productId identifier of the product
     * @param newStock  new available units
     * @return the current or updated product
     * @throws ResourceNotFoundException
     * when the product does not exist
     * @throws com.andresyfr.franchise.domain.exception.BusinessRuleViolationException
     * when the stock is negative
     */
    public Mono<Product> execute(String productId, int newStock) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(() -> new ResourceNotFoundException(Product.RESOURCE_NAME, productId)))
                .flatMap(current -> {
                    Product updated = current.updateStock(newStock);
                    return current.stock() == updated.stock()
                            ? Mono.just(current)
                            : productRepository.save(updated);
                });
    }
}
