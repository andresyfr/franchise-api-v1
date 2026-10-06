package com.andresyfr.franchise.infrastructure.config;

import com.andresyfr.franchise.application.usecase.CreateFranchiseUseCase;
import com.andresyfr.franchise.application.usecase.RenameFranchiseUseCase;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Connects framework-free use cases to their output ports.
 */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

    @Bean
    public CreateFranchiseUseCase createFranchiseUseCase(FranchiseRepository franchiseRepository) {
        return new CreateFranchiseUseCase(franchiseRepository);
    }

    @Bean
    public RenameFranchiseUseCase renameFranchiseUseCase(FranchiseRepository franchiseRepository) {
        return new RenameFranchiseUseCase(franchiseRepository);
    }
}