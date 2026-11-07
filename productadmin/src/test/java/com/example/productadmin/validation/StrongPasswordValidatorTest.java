package com.example.productadmin.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Secret123", "abcdefg1", "1234567a", "Admin@123"})
    void acceptsStrongPasswords(String password) {
        assertThat(validator.isValid(password, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"short1", "onlyletters", "12345678", ""})
    void rejectsWeakPasswords(String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }

    @Test
    void rejectsTooLongPassword() {
        assertThat(validator.isValid("a1".repeat(40), null)).isFalse();
    }

    @Test
    void nullIsLeftToNotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
