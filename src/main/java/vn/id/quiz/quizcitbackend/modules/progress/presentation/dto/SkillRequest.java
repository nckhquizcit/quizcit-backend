package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request tạo / cập nhật một {@link Skill}.
 *
 * <p>Phase 2 (P1) - CRUD API cho Skill.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest {

    @NotBlank(message = "Tên kỹ năng không được để trống")
    @Size(max = 200, message = "Tên kỹ năng tối đa 200 ký tự")
    private String name;

    @Size(max = 2000, message = "Mô tả kỹ năng tối đa 2000 ký tự")
    private String description;
}
