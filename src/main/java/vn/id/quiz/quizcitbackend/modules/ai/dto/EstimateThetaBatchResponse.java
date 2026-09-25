package vn.id.quiz.quizcitbackend.modules.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response batch từ AI Service ({@code AI/main.py} -> {@code /estimate-batch}).
 *
 * <p>Mỗi {@link Item} chứa {@code key} (skillId) để backend map lại kết quả,
 * cùng {@code theta} raw. {@code theta_scaled} không bắt buộc — backend sẽ
 * tự scale từ theta raw để đảm bảo nhất quán với đường single {@code /estimate}.</p>
 *
 * @param results danh sách kết quả theo từng item
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EstimateThetaBatchResponse(
        @JsonProperty("results") List<Item> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            @JsonProperty("key") String key,
            @JsonProperty("theta") double theta,
            @JsonProperty("theta_scaled") double thetaScaled,
            @JsonProperty("theta_variance") double thetaVariance) {
    }
}
