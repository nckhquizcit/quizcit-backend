package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.util.List;

public record CreateChapterRequest (

        @NotBlank(message="Tên chương không được để trống")
        @Size(max=200, message="Tên chương không được vượt quá 200 ký tự")
        String name,
        Integer displayOrder,
        @Valid List<ChapterSkillRequest> skills
){
}
