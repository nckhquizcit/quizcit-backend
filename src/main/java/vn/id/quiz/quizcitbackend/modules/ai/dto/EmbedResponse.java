package vn.id.quiz.quizcitbackend.modules.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response từ AI Service ({@code /embed}). Mỗi phần tử của {@code embeddings}
 * là 1 vector float tương ứng với 1 phần tử trong {@code texts} của request,
 * theo đúng thứ tự.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmbedResponse(
        @JsonProperty("embeddings") List<List<Float>> embeddings) {
}
