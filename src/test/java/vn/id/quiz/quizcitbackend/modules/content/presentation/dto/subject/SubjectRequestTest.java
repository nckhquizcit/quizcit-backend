package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

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

class SubjectRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validCreateSubjectRequestShouldHaveNoViolations() {
        CreateSubjectRequest request = new CreateSubjectRequest(
                "CT101",
                "Lập trình hướng đối tượng",
                "Mô tả môn học",
                1,
                "https://example.com/image.png"
        );

        Set<ConstraintViolation<CreateSubjectRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CS 101", "CS\t101", "toán học", "môn_học_1", "CT-101@"})
    void invalidCourseCodeShouldFail(String code) {
        CreateSubjectRequest request = new CreateSubjectRequest(
                code,
                "Tên hợp lệ",
                "Mô tả",
                1,
                null
        );

        Set<ConstraintViolation<CreateSubjectRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("courseCode") &&
                (v.getMessage().equals(ErrorMessages.COURSE_CODE_INVALID) ||
                 v.getMessage().equals(ErrorMessages.COURSE_CODE_LENGTH))
        );
    }
}
