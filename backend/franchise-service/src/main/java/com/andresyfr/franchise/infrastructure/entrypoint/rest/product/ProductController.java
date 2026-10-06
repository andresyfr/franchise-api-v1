package com.andresyfr.franchise.infrastructure.entrypoint.rest.product;

import com.andresyfr.franchise.application.usecase.AddProductUseCase;
import com.andresyfr.franchise.application.usecase.RemoveProductUseCase;
import com.andresyfr.franchise.application.usecase.RenameProductUseCase;
import com.andresyfr.franchise.application.usecase.UpdateProductStockUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * HTTP adapter for product commands.
 *
 * <p>The controller validates transport data and delegates every business
 * decision to the use cases.</p>
 */
@Tag(name = "Products", description = "Product and stock management")
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final AddProductUseCase addProductUseCase;
    private final RemoveProductUseCase removeProductUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final RenameProductUseCase renameProductUseCase;

    /**
     * Creates the controller.
     *
     * @param addProductUseCase         use case that adds products
     * @param removeProductUseCase      use case that removes products
     * @param updateProductStockUseCase use case that updates stock
     * @param renameProductUseCase      use case that renames products
     */
    public ProductController(AddProductUseCase addProductUseCase,
                             RemoveProductUseCase removeProductUseCase,
                             UpdateProductStockUseCase updateProductStockUseCase,
                             RenameProductUseCase renameProductUseCase) {
        this.addProductUseCase = addProductUseCase;
        this.removeProductUseCase = removeProductUseCase;
        this.updateProductStockUseCase = updateProductStockUseCase;
        this.renameProductUseCase = renameProductUseCase;
    }

    /**
     * Adds a product to a branch.
     *
     * @param branchId identifier of the branch
     * @param request  validated request body
     * @return the created product with HTTP status 201
     */
    @Operation(summary = "Add a product to a branch")
    @PostMapping(value = "/branches/{branchId}/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductResponse> add(@PathVariable String branchId,
                                     @Valid @RequestBody AddProductRequest request) {
        return addProductUseCase.execute(branchId, request.name(), request.stock())
                .map(ProductResponse::from);
    }

    /**
     * Removes a product from a branch.
     *
     * @param branchId  identifier of the branch that owns the product
     * @param productId identifier of the product
     * @return an empty response with HTTP status 204
     */
    @Operation(summary = "Remove a product from a branch")
    @DeleteMapping("/branches/{branchId}/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> remove(@PathVariable String branchId, @PathVariable String productId) {
        return removeProductUseCase.execute(branchId, productId);
    }

    /**
     * Replaces the stock of a product.
     *
     * @param productId identifier of the product
     * @param request   validated request body
     * @return the updated product
     */
    @Operation(summary = "Update the stock of a product")
    @PatchMapping(value = "/products/{productId}/stock", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProductResponse> updateStock(@PathVariable String productId,
                                             @Valid @RequestBody UpdateStockRequest request) {
        return updateProductStockUseCase.execute(productId, request.stock())
                .map(ProductResponse::from);
    }

    /**
     * Renames a product.
     *
     * @param productId identifier of the product
     * @param request   validated request body
     * @return the updated product
     */
    @Operation(summary = "Rename a product")
    @PatchMapping(value = "/products/{productId}/name", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProductResponse> rename(@PathVariable String productId,
                                        @Valid @RequestBody RenameProductRequest request) {
        return renameProductUseCase.execute(productId, request.name())
                .map(ProductResponse::from);
    }
}