package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BulkCreateSkillsRequest(
        @NotEmpty(message = "Danh sách kỹ năng không được để trống")
        @Size(max = 500, message = "Mỗi lần chỉ được import tối đa 500 kỹ năng")
        List<BulkCreateSkillItemRequest> items) {
}
