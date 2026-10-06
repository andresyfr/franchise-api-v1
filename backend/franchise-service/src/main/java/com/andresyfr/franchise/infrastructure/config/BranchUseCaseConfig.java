package com.andresyfr.franchise.infrastructure.config;

import com.andresyfr.franchise.application.usecase.AddBranchUseCase;
import com.andresyfr.franchise.application.usecase.RenameBranchUseCase;
import com.andresyfr.franchise.domain.port.BranchRepository;
import com.andresyfr.franchise.domain.port.FranchiseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the framework-free branch use cases as Spring beans.
 *
 * <p>Kept separate from the franchise configuration so each feature owns its
 * wiring and can evolve independently.</p>
 */
@Configuration(proxyBeanMethods = false)
public class BranchUseCaseConfig {

    /**
     * Creates the use case that adds branches to a franchise.
     *
     * @param franchiseRepository franchise output port
     * @param branchRepository    branch output port
     * @return configured use case
     */
    @Bean
    public AddBranchUseCase addBranchUseCase(FranchiseRepository franchiseRepository,
                                             BranchRepository branchRepository) {
        return new AddBranchUseCase(franchiseRepository, branchRepository);
    }

    /**
     * Creates the use case that renames branches.
     *
     * @param branchRepository branch output port
     * @return configured use case
     */
    @Bean
    public RenameBranchUseCase renameBranchUseCase(BranchRepository branchRepository) {
        return new RenameBranchUseCase(branchRepository);
    }
}