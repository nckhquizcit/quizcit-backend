package vn.id.quiz.quizcitbackend.modules.ai.dto;

import java.util.List;

public record EstimateThetaRequest(
        List<Integer> responses,
        List<Double> difficulties,
        List<Double> weights) {
}