package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillThetaData {

    private List<Integer> responses = new ArrayList<>();

    private List<Double> difficulties = new ArrayList<>();

    private List<Double> weights = new ArrayList<>();
}