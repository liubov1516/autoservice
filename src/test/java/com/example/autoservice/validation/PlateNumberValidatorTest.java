package com.example.autoservice.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlateNumberValidatorTest {

    private final PlateNumberValidator validator = new PlateNumberValidator();

    @Test
    void isValidCorrectPlateFormatReturnsTrue() {
        boolean result = validator.isValid("AA1234BB", null);

        assertThat(result).isTrue();
    }

    @Test
    void isValidWrongPlateFormatReturnsFalse() {
        boolean result = validator.isValid("12AABBAA", null);

        assertThat(result).isFalse();
    }

    @Test
    void isValidBlankValueReturnsTrue() {
        boolean result = validator.isValid("", null);

        assertThat(result).isTrue();
    }
}
