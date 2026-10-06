package com.andresyfr.franchise.infrastructure.config;

import com.andresyfr.franchise.application.usecase.GetTopStockProductsUseCase;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import com.andresyfr.franchise.domain.port.TopStockProductQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the highest-stock query use case as a Spring bean.
 */
@Configuration(proxyBeanMethods = false)
public class TopStockUseCaseConfig {

    /**
     * Creates the use case that lists the highest-stock product per branch.
     *
     * @param franchiseRepository  franchise output port
     * @param topStockProductQuery highest-stock read port
     * @return configured use case
     */
    @Bean
    public GetTopStockProductsUseCase getTopStockProductsUseCase(FranchiseRepository franchiseRepository,
                                                                 TopStockProductQuery topStockProductQuery) {
        return new GetTopStockProductsUseCase(franchiseRepository, topStockProductQuery);
    }
}
