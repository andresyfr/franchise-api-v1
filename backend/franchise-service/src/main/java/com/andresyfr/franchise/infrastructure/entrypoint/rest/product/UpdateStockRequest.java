package com.andresyfr.franchise.infrastructure.entrypoint.rest.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request body to replace the stock of a product.
 *
 * @param stock new stock, zero or greater
 */
public record UpdateStockRequest(
        @Schema(example = "40")
        @NotNull
        @PositiveOrZero
        Integer stock) {
}