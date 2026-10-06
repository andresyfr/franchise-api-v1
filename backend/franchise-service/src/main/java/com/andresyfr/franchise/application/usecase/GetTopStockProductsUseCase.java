package com.andresyfr.franchise.application.usecase;

import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Franchise;
import com.andresyfr.franchise.domain.model.TopStockProduct;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import com.andresyfr.franchise.domain.port.TopStockProductQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Returns the product with the highest stock for each branch of a franchise.
 */
public class GetTopStockProductsUseCase {

    private final FranchiseRepository franchiseRepository;
    private final TopStockProductQuery topStockProductQuery;

    /**
     * Creates the use case.
     *
     * @param franchiseRepository  port used to verify that the franchise exists
     * @param topStockProductQuery read port that resolves the highest-stock products
     */
    public GetTopStockProductsUseCase(FranchiseRepository franchiseRepository,
                                      TopStockProductQuery topStockProductQuery) {
        this.franchiseRepository = franchiseRepository;
        this.topStockProductQuery = topStockProductQuery;
    }

    /**
     * Lists the highest-stock products of every branch of the franchise.
     *
     * <p>An existing franchise without branches or products produces an empty
     * result instead of an error.</p>
     *
     * @param franchiseId identifier of the franchise
     * @return the highest-stock product of each branch, including ties
     * @throws ResourceNotFoundException when the franchise does not exist
     */
    public Flux<TopStockProduct> execute(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() ->
                        new ResourceNotFoundException(Franchise.RESOURCE_NAME, franchiseId)))
                .thenMany(Flux.defer(() -> topStockProductQuery.findByFranchiseId(franchiseId)));
    }
}
