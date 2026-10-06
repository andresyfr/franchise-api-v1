package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.domain.port.BranchRepository;
import com.andresyfr.franchise.domain.port.ProductRepository;
import reactor.core.publisher.Mono;

/**
 * Adds a new product to an existing branch.
 *
 * <p>Order of checks: name and stock validity, branch existence and name
 * uniqueness within the branch. Invalid input is rejected before any database call.</p>
 */
public class AddProductUseCase {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    /**
     * Creates the use case.
     *
     * @param branchRepository  port used to verify that the branch exists
     * @param productRepository port used to check uniqueness and persist the product
     */
    public AddProductUseCase(BranchRepository branchRepository, ProductRepository productRepository) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
    }

    /**
     * Validates, creates and persists a product for the given branch.
     *
     * @param branchId identifier of the branch
     * @param name     proposed product name
     * @param stock    initial stock
     * @return the persisted product
     * @throws com.andresyfr.franchise.domain.exception.BusinessRuleViolationException
     * when the name or the stock is invalid
     * @throws ResourceNotFoundException
     * when the branch does not exist
     * @throws com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException
     * when the branch already offers a product with that name
     */
    public Mono<Product> execute(String branchId, String name, int stock) {
        return Mono.fromCallable(() -> Product.create(branchId, name, stock))
                .flatMap(product -> branchRepository.findById(branchId)
                        .switchIfEmpty(Mono.error(() ->
                                new ResourceNotFoundException(Branch.RESOURCE_NAME, branchId)))
                        .then(Mono.defer(() ->
                                ProductNameGuard.ensureAvailable(productRepository, branchId, product.name())))
                        .then(Mono.defer(() -> productRepository.save(product))));
    }
}
