package com.andresyfr.franchise.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FranchiseTest {

    @Test
    void createShouldGenerateIdAndTrimName() {
        Franchise franchise = Franchise.create("  Franquicia Colombia  ");

        assertThat(franchise.id()).isNotBlank();
        assertThat(franchise.name()).isEqualTo("Franquicia Colombia");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void createShouldRejectBlankNames(String name) {
        assertThatThrownBy(() -> Franchise.create(name))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Franchise name must not be blank");
    }

    @Test
    void createShouldRejectNamesLongerThanMaximum() {
        String tooLong = "a".repeat(Franchise.MAX_NAME_LENGTH + 1);

        assertThatThrownBy(() -> Franchise.create(tooLong))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void renameShouldKeepIdAndReturnNewInstance() {
        Franchise original = Franchise.create("Original");

        Franchise renamed = original.rename("Renamed");

        assertThat(renamed.id()).isEqualTo(original.id());
        assertThat(renamed.name()).isEqualTo("Renamed");
        assertThat(original.name()).isEqualTo("Original");
    }
}
