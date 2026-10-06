package com.andresyfr.franchise.infrastructure.config;

import com.andresyfr.franchise.application.usecase.AddProductUseCase;
import com.andresyfr.franchise.application.usecase.RemoveProductUseCase;
import com.andresyfr.franchise.application.usecase.RenameProductUseCase;
import com.andresyfr.franchise.application.usecase.UpdateProductStockUseCase;
import com.andresyfr.franchise.domain.port.BranchRepository;
import com.andresyfr.franchise.domain.port.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the framework-free product use cases as Spring beans.
 */
@Configuration(proxyBeanMethods = false)
public class ProductUseCaseConfig {

    /**
     * Creates the use case that adds products to a branch.
     *
     * @param branchRepository  branch output port
     * @param productRepository product output port
     * @return configured use case
     */
    @Bean
    public AddProductUseCase addProductUseCase(BranchRepository branchRepository,
                                               ProductRepository productRepository) {
        return new AddProductUseCase(branchRepository, productRepository);
    }

    /**
     * Creates the use case that removes products from a branch.
     *
     * @param productRepository product output port
     * @return configured use case
     */
    @Bean
    public RemoveProductUseCase removeProductUseCase(ProductRepository productRepository) {
        return new RemoveProductUseCase(productRepository);
    }

    /**
     * Creates the use case that updates product stock.
     *
     * @param productRepository product output port
     * @return configured use case
     */
    @Bean
    public UpdateProductStockUseCase updateProductStockUseCase(ProductRepository productRepository) {
        return new UpdateProductStockUseCase(productRepository);
    }

    /**
     * Creates the use case that renames products.
     *
     * @param productRepository product output port
     * @return configured use case
     */
    @Bean
    public RenameProductUseCase renameProductUseCase(ProductRepository productRepository) {
        return new RenameProductUseCase(productRepository);
    }
}