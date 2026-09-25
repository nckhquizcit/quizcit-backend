package vn.id.quiz.quizcitbackend.modules.ai.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request gọi AI Service ước lượng theta cho NHIỀU skill trong 1 lần
 * (batch) — thay cho việc gọi {@code /estimate} lặp lại cho từng skill.
 *
 * <p>Mỗi {@link Item} tương ứng một skill, được nhận diện qua {@code key}
 * (thường là {@code skillId}) để phía backend map kết quả trả về.</p>
 *
 * @param items danh sách item, mỗi item là dữ liệu 1 skill
 */
public record EstimateThetaBatchRequest(
        @NotNull @NotEmpty List<Item> items) {

    /**
     * @param key          định danh item (skillId) để map kết quả
     * @param oldTheta     theta cũ
     * @param responses    danh sách đáp án đúng (1) / sai (0)
     * @param difficulties danh sách độ khó (b) tương ứng từng câu
     * @param weights      danh sách trọng số tương ứng từng câu
     */
    public record Item(
            @NotNull String key,
            double oldTheta,
            double oldThetaVar,
            @NotNull @NotEmpty List<Integer> responses,
            @NotNull @NotEmpty List<Double> difficulties,
            @NotNull @NotEmpty List<Double> weights) {
    }
}
