package com.andresyfr.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;
import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import com.andresyfr.franchise.domain.model.Branch;
import com.andresyfr.franchise.domain.model.Product;
import com.andresyfr.franchise.support.InMemoryBranchRepository;
import com.andresyfr.franchise.support.InMemoryProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

class AddProductUseCaseTest {

    private InMemoryBranchRepository branchRepository;
    private InMemoryProductRepository productRepository;
    private AddProductUseCase useCase;
    private Branch branch;

    @BeforeEach
    void setUp() {
        branchRepository = new InMemoryBranchRepository();
        productRepository = new InMemoryProductRepository();
        useCase = new AddProductUseCase(branchRepository, productRepository);
        branch = branchRepository.store(Branch.create("franchise-1", "Sucursal Centro"));
    }

    @Test
    void shouldAddProductToExistingBranch() {
        StepVerifier.create(useCase.execute(branch.id(), "Hamburguesa", 25))
                .assertNext(product -> {
                    assertThat(product.branchId()).isEqualTo(branch.id());
                    assertThat(product.name()).isEqualTo("Hamburguesa");
                    assertThat(product.stock()).isEqualTo(25);
                })
                .verifyComplete();

        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldFailWhenBranchDoesNotExist() {
        StepVerifier.create(useCase.execute("missing-branch", "Hamburguesa", 25))
                .expectError(ResourceNotFoundException.class)
                .verify();

        assertThat(productRepository.count()).isZero();
    }

    @Test
    void shouldRejectDuplicatedNameWithinSameBranch() {
        productRepository.store(Product.create(branch.id(), "Hamburguesa", 1));

        StepVerifier.create(useCase.execute(branch.id(), " Hamburguesa ", 5))
                .expectError(ResourceAlreadyExistsException.class)
                .verify();
    }

    @Test
    void shouldAllowSameNameInDifferentBranches() {
        productRepository.store(Product.create("other-branch", "Hamburguesa", 1));

        StepVerifier.create(useCase.execute(branch.id(), "Hamburguesa", 5))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldRejectNegativeStockBeforeQueryingRepositories() {
        StepVerifier.create(useCase.execute("missing-branch", "Hamburguesa", -1))
                .expectError(BusinessRuleViolationException.class)
                .verify();
    }
}
