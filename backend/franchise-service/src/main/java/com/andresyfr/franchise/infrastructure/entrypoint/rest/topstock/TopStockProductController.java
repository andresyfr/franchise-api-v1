package com.andresyfr.franchise.infrastructure.entrypoint.rest.topstock;

import com.andresyfr.franchise.application.usecase.GetTopStockProductsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * HTTP adapter for the highest-stock query of a franchise.
 */
@Tag(name = "Franchises", description = "Franchise management")
@RestController
@RequestMapping(value = "/api/v1/franchises", produces = MediaType.APPLICATION_JSON_VALUE)
public class TopStockProductController {

    private final GetTopStockProductsUseCase getTopStockProductsUseCase;

    /**
     * Creates the controller.
     *
     * @param getTopStockProductsUseCase use case that resolves the highest-stock products
     */
    public TopStockProductController(GetTopStockProductsUseCase getTopStockProductsUseCase) {
        this.getTopStockProductsUseCase = getTopStockProductsUseCase;
    }

    /**
     * Lists the product with the highest stock for each branch of a franchise.
     *
     * @param franchiseId identifier of the franchise
     * @return one entry per branch, or several when products share the highest stock
     */
    @Operation(summary = "Get the product with the highest stock per branch of a franchise")
    @GetMapping("/{franchiseId}/top-stock-products")
    public Flux<TopStockProductResponse> getTopStockProducts(@PathVariable String franchiseId) {
        return getTopStockProductsUseCase.execute(franchiseId)
                .map(TopStockProductResponse::from);
    }
}