package vn.id.quiz.quizcitbackend.modules.ai.client;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AIClient {

    private final RestClient aiRestClient;

    public double estimateTheta(
            EstimateThetaRequest request) {

        EstimateThetaResponse response = aiRestClient.post()
                .uri("/estimate")
                .body(request)
                .retrieve()
                .body(EstimateThetaResponse.class);

        return response.theta();
    }
}