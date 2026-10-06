package com.andresyfr.franchise.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class BranchTest {

    private static final String FRANCHISE_ID = "franchise-1";

    @Test
    void createShouldGenerateIdKeepFranchiseAndTrimName() {
        Branch branch = Branch.create(FRANCHISE_ID, "  Sucursal Centro  ");

        assertThat(branch.id()).isNotBlank();
        assertThat(branch.franchiseId()).isEqualTo(FRANCHISE_ID);
        assertThat(branch.name()).isEqualTo("Sucursal Centro");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void createShouldRejectBlankNames(String name) {
        assertThatThrownBy(() -> Branch.create(FRANCHISE_ID, name))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Branch name must not be blank");
    }

    @Test
    void createShouldRejectNamesLongerThanMaximum() {
        String tooLong = "a".repeat(Branch.MAX_NAME_LENGTH + 1);

        assertThatThrownBy(() -> Branch.create(FRANCHISE_ID, tooLong))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createShouldRequireFranchise() {
        assertThatThrownBy(() -> Branch.create(null, "Sucursal"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void renameShouldKeepIdAndFranchise() {
        Branch original = Branch.create(FRANCHISE_ID, "Original");

        Branch renamed = original.rename("Renamed");

        assertThat(renamed.id()).isEqualTo(original.id());
        assertThat(renamed.franchiseId()).isEqualTo(FRANCHISE_ID);
        assertThat(renamed.name()).isEqualTo("Renamed");
        assertThat(original.name()).isEqualTo("Original");
    }
}
