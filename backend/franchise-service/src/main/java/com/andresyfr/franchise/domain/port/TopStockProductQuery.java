package com.andresyfr.franchise.domain.port;

import com.andresyfr.franchise.domain.model.TopStockProduct;
import reactor.core.publisher.Flux;

/**
 * Reactive query port that returns the highest-stock products of a franchise.
 *
 * <p>It is a read-only port, separated from the write repositories, so the
 * persistence adapter can resolve the query in a single database round trip.</p>
 */
public interface TopStockProductQuery {

    /**
     * Finds, for every branch of the franchise, the products with the highest stock.
     *
     * <p>Branches without products are omitted. Ties are all returned. Results are
     * ordered by branch name and then by product name.</p>
     *
     * @param franchiseId identifier of the franchise
     * @return the highest-stock products, or an empty publisher when none exist
     */
    Flux<TopStockProduct> findByFranchiseId(String franchiseId);
}
