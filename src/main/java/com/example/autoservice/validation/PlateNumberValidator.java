package com.example.autoservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

// Клас-валідатор для @ValidPlateNumber.
// Spring/Bean Validation знаходить і викликає цей клас автоматично - через validatedBy у анотації.
public class PlateNumberValidator implements ConstraintValidator<ValidPlateNumber, String> {

    // 2 літери, 4 цифри, 2 літери (латиниця), наприклад AA1234BB
    private static final Pattern PLATE_PATTERN = Pattern.compile("^[A-Z]{2}\\d{4}[A-Z]{2}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // порожнє значення тут вважаємо валідним - за обов'язковість поля відповідає @NotBlank
        if (value == null || value.isBlank()) {
            return true;
        }
        return PLATE_PATTERN.matcher(value.trim()).matches();
    }
}
