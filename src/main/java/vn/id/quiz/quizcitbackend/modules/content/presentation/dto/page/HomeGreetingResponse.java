package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page;

import lombok.Builder;

@Builder
public record HomeGreetingResponse(
        String period,
        String displayName,
        String username,
        String firstName
) {
}
