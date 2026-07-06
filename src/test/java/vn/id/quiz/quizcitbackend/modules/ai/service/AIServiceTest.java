package vn.id.quiz.quizcitbackend.modules.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import vn.id.quiz.quizcitbackend.modules.ai.client.AIClient;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;

class AIServiceTest {

    @Test
    void estimateThetaShouldAddScaledThetaForRemoteResponse() {
        EstimateThetaRequest request = request(List.of(1), List.of(0.0), List.of(1.0));
        AIService service = new AIService(new FakeAIClient(new EstimateThetaResponse(0.0, 0.0)));

        EstimateThetaResponse response = service.estimateTheta(request);

        assertThat(response.theta()).isEqualTo(0.0);
        assertThat(response.thetaScaled()).isEqualTo(2.0);
    }

    @Test
    void estimateThetaShouldUseLocalFallbackWhenRemoteServiceFails() {
        EstimateThetaRequest request = request(
                List.of(1, 1, 1),
                List.of(0.0, 0.5, 1.0),
                List.of(1.0, 1.0, 1.0));
        AIService service = new AIService(new FakeAIClient(new RuntimeException("connection refused")));

        EstimateThetaResponse response = service.estimateTheta(request);

        assertThat(response.theta()).isGreaterThan(0.0);
        assertThat(response.thetaScaled()).isGreaterThan(2.0);
    }

    private EstimateThetaRequest request(
            List<Integer> responses,
            List<Double> difficulties,
            List<Double> weights) {

        return new EstimateThetaRequest(responses, difficulties, weights);
    }

    private static final class FakeAIClient extends AIClient {

        private final EstimateThetaResponse response;
        private final RuntimeException exception;

        private FakeAIClient(EstimateThetaResponse response) {
            super(null);
            this.response = response;
            this.exception = null;
        }

        private FakeAIClient(RuntimeException exception) {
            super(null);
            this.response = null;
            this.exception = exception;
        }

        @Override
        public EstimateThetaResponse estimateTheta(
                EstimateThetaRequest request) {

            if (exception != null) {
                throw exception;
            }

            return response;
        }
    }
}
