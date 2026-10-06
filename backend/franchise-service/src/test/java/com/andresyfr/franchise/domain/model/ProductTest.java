package com.andresyfr.franchise.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {

    private static final String BRANCH_ID = "branch-1";

    @Test
    void createShouldGenerateIdAndTrimName() {
        Product product = Product.create(BRANCH_ID, "  Hamburguesa  ", 10);

        assertThat(product.id()).isNotBlank();
        assertThat(product.branchId()).isEqualTo(BRANCH_ID);
        assertThat(product.name()).isEqualTo("Hamburguesa");
        assertThat(product.stock()).isEqualTo(10);
    }

    @Test
    void createShouldAcceptZeroStock() {
        assertThat(Product.create(BRANCH_ID, "Agotado", 0).stock()).isZero();
    }

    @Test
    void createShouldRejectNegativeStock() {
        assertThatThrownBy(() -> Product.create(BRANCH_ID, "Hamburguesa", -1))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Stock must be zero or greater");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void createShouldRejectBlankNames(String name) {
        assertThatThrownBy(() -> Product.create(BRANCH_ID, name, 1))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Product name must not be blank");
    }

    @Test
    void updateStockShouldKeepOtherFields() {
        Product original = Product.create(BRANCH_ID, "Hamburguesa", 10);

        Product updated = original.updateStock(30);

        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.name()).isEqualTo("Hamburguesa");
        assertThat(updated.stock()).isEqualTo(30);
        assertThat(original.stock()).isEqualTo(10);
    }

    @Test
    void updateStockShouldRejectNegativeValues() {
        Product product = Product.create(BRANCH_ID, "Hamburguesa", 10);

        assertThatThrownBy(() -> product.updateStock(-5))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void renameShouldKeepIdBranchAndStock() {
        Product original = Product.create(BRANCH_ID, "Original", 7);

        Product renamed = original.rename("Renamed");

        assertThat(renamed.id()).isEqualTo(original.id());
        assertThat(renamed.branchId()).isEqualTo(BRANCH_ID);
        assertThat(renamed.stock()).isEqualTo(7);
        assertThat(renamed.name()).isEqualTo("Renamed");
    }

    @Test
    void belongsToShouldCompareBranch() {
        Product product = Product.create(BRANCH_ID, "Hamburguesa", 1);

        assertThat(product.belongsTo(BRANCH_ID)).isTrue();
        assertThat(product.belongsTo("other-branch")).isFalse();
    }
}
