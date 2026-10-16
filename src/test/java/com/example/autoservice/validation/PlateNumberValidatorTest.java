package com.example.autoservice.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Тест класу-валідатора напряму, без моків (чиста логіка, залежностей немає)
class PlateNumberValidatorTest {

    private final PlateNumberValidator validator = new PlateNumberValidator();

    @Test
    void isValidCorrectPlateFormatReturnsTrue() {
        // Arrange + Act
        boolean result = validator.isValid("AA1234BB", null);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isValidWrongPlateFormatReturnsFalse() {
        // Arrange + Act: цифри й літери переплутані місцями
        boolean result = validator.isValid("12AABBAA", null);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isValidBlankValueReturnsTrue() {
        // Arrange + Act: порожнє значення - за обов'язковість відповідає @NotBlank, тут вважаємо валідним
        boolean result = validator.isValid("", null);

        // Assert
        assertThat(result).isTrue();
    }
}
