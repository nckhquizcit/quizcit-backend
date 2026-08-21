package vn.id.quiz.quizcitbackend.modules.progress.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.BulkCreateSkillItemRequest;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.BulkCreateSkillsRequest;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.BulkCreateSkillsResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillRequest;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillResponse;
import vn.id.quiz.quizcitbackend.modules.progress.service.SkillService;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

class SkillControllerTest {

    private SkillService skillService;
    private Validator validator;
    private SkillController controller;

    @BeforeEach
    void setUp() {
        skillService = mock(SkillService.class);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        controller = new SkillController(skillService, validator);
    }

    @Test
    void createSkillsBulkHandlesSuccessAndValidationFailures() {
        SkillRequest validSkill = SkillRequest.builder()
                .name("Đạo hàm hàm hợp")
                .description("Mô tả kỹ năng")
                .build();

        SkillResponse mockResponse = SkillResponse.builder()
                .skillId("skill-123")
                .name("Đạo hàm hàm hợp")
                .description("Mô tả kỹ năng")
                .build();

        when(skillService.createSkill(any(SkillRequest.class))).thenReturn(mockResponse);

        BulkCreateSkillItemRequest item1 = new BulkCreateSkillItemRequest("item-1", validSkill);
        BulkCreateSkillItemRequest item2 = new BulkCreateSkillItemRequest("item-2", SkillRequest.builder().name("").build());

        BulkCreateSkillsRequest request = new BulkCreateSkillsRequest(List.of(item1, item2));

        var responseEntity = controller.createSkillsBulk(request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        BulkCreateSkillsResponse data = responseEntity.getBody().data();
        assertThat(data.total()).isEqualTo(2);
        assertThat(data.successCount()).isEqualTo(1);
        assertThat(data.failureCount()).isEqualTo(1);

        assertThat(data.items().get(0).success()).isTrue();
        assertThat(data.items().get(0).skill().getSkillId()).isEqualTo("skill-123");

        assertThat(data.items().get(1).success()).isFalse();
        assertThat(data.items().get(1).message()).isNotBlank();
    }

    @Test
    void createSkillsBulkHandlesServiceExceptionGracefully() {
        SkillRequest validSkill = SkillRequest.builder()
                .name("Kỹ năng lỗi")
                .build();

        when(skillService.createSkill(any(SkillRequest.class)))
                .thenThrow(new AppException("Lỗi hệ thống", HttpStatus.BAD_REQUEST));

        BulkCreateSkillItemRequest item = new BulkCreateSkillItemRequest("item-1", validSkill);
        BulkCreateSkillsRequest request = new BulkCreateSkillsRequest(List.of(item));

        var responseEntity = controller.createSkillsBulk(request);
        BulkCreateSkillsResponse data = responseEntity.getBody().data();

        assertThat(data.successCount()).isEqualTo(0);
        assertThat(data.failureCount()).isEqualTo(1);
        assertThat(data.items().get(0).success()).isFalse();
        assertThat(data.items().get(0).message()).contains("Lỗi hệ thống");
    }
}
