package vn.id.quiz.quizcitbackend.shared.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReorderRequest(
        @NotEmpty List<@Valid ReorderItemRequest> items
) {}
