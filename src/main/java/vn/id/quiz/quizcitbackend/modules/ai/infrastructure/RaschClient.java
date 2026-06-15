package vn.id.quiz.quizcitbackend.modules.ai.infrastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RaschClient {

    private final RestTemplate restTemplate;

    public RaschClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public Double estimateTheta(
            List<Integer> responses,
            List<Double> difficulties,
            List<Double> weights) {

        Map<String, Object> request = new HashMap<>();

        request.put("responses", responses);
        request.put("difficulties", difficulties);
        request.put("weights", weights);

        String url = "http://localhost:8000/estimate";

        ResponseEntity<RaschResponse> response = restTemplate.postForEntity(
                url,
                request,
                RaschResponse.class);

        return response.getBody().getTheta();
    }
}