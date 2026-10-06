package com.andresyfr.franchise.infrastructure.entrypoint.rest.product;

import com.andresyfr.franchise.domain.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body to rename a product.
 *
 * @param name proposed new product name
 */
public record RenameProductRequest(
        @Schema(example = "Hamburguesa doble")
        @NotBlank
        @Size(max = Product.MAX_NAME_LENGTH)
        String name) {
}
