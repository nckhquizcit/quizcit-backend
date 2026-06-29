package vn.id.quiz.quizcitbackend.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response từ AI Service.
 *
 * <p>Các field map đúng với contract của AI Service ({@code quizcit-ai/main.py}
 * -> {@code rasch_theta}) ở dạng snake_case:
 * <ul>
 *   <li>{@code theta} - logit gốc [-4, 4], dùng cho tính toán/calibration.</li>
 *   <li>{@code theta_scaled} - điểm hiển thị [0, 4] cho FE.</li>
 * </ul>
 * </p>
 *
 * @param theta       năng lực ước lượng (theta) của người dùng
 * @param thetaScaled theta đã scale sang [0, 4] để hiển thị
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EstimateThetaResponse(
        @JsonProperty("theta") double theta,
        @JsonProperty("theta_scaled") double thetaScaled
) {
}
