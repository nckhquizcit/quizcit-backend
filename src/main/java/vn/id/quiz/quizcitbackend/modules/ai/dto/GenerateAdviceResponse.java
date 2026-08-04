package vn.id.quiz.quizcitbackend.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateAdviceResponse(
        @JsonProperty("advice") String advice) {
}
