package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRegisterRequestShouldHaveNoViolations() {
        RegisterRequest request = new RegisterRequest(
                "student01",
                "password123",
                "student01@example.com",
                "Student One"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user_name", "user.name", "user-name", "student_01", "user.name-123"})
    void validUsernameFormatsShouldPass(String username) {
        RegisterRequest request = new RegisterRequest(
                username,
                "password123",
                "student@example.com",
                "Student"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"student 01", " student01", "student01 ", "student\t01", "student  01"})
    void usernameWithSpacesShouldFail(String username) {
        RegisterRequest request = new RegisterRequest(
                username,
                "password123",
                "student@example.com",
                "Student"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("username") &&
                v.getMessage().equals(ErrorMessages.USERNAME_INVALID)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nguyễn_văn_a",
            "đỗ_nam_trung",
            "hoàng_huân",
            "tiếngviệt",
            "bàitập",
            "phạm_văn_đồng",
            "lương_thế_vinh"
    })
    void usernameWithVietnameseAccentsShouldFail(String username) {
        RegisterRequest request = new RegisterRequest(
                username,
                "password123",
                "student@example.com",
                "Student"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("username") &&
                v.getMessage().equals(ErrorMessages.USERNAME_INVALID)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"student@01", "user#1", "user$name", "user!name", "user%test", "user^name"})
    void usernameWithSpecialSymbolsShouldFail(String username) {
        RegisterRequest request = new RegisterRequest(
                username,
                "password123",
                "student@example.com",
                "Student"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("username") &&
                v.getMessage().equals(ErrorMessages.USERNAME_INVALID)
        );
    }

    @Test
    void usernameTooShortShouldFail() {
        RegisterRequest request = new RegisterRequest(
                "ab",
                "password123",
                "student@example.com",
                "Student"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("username") &&
                v.getMessage().equals(ErrorMessages.USERNAME_LENGTH)
        );
    }

    @Test
    void usernameTooLongShouldFail() {
        RegisterRequest request = new RegisterRequest(
                "a".repeat(51),
                "password123",
                "student@example.com",
                "Student"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("username") &&
                v.getMessage().equals(ErrorMessages.USERNAME_LENGTH)
        );
    }

    @Test
    void blankUsernameShouldFail() {
        RegisterRequest request = new RegisterRequest(
                "",
                "password123",
                "student@example.com",
                "Student"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("username") &&
                v.getMessage().equals(ErrorMessages.USERNAME_REQUIRED)
        );
    }
}
