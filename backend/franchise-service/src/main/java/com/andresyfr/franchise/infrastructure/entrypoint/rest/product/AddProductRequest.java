package com.andresyfr.franchise.infrastructure.entrypoint.rest.product;

import com.andresyfr.franchise.domain.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request body to add a product to a branch.
 *
 * @param name  proposed product name
 * @param stock initial stock, zero or greater
 */
public record AddProductRequest(
        @Schema(example = "Hamburguesa clásica")
        @NotBlank
        @Size(max = Product.MAX_NAME_LENGTH)
        String name,

        @Schema(example = "25")
        @NotNull
        @PositiveOrZero
        Integer stock) {
}