package vn.id.quiz.quizcitbackend.modules.content.application.service;

import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateTheoryRequest;

import java.util.List;

public interface TheoryService {
    List<TheoryResponse> getAllTheories();
    TheoryResponse getTheory(String theoryId);
    TheoryResponse createTheory(CreateTheoryRequest request);
    TheoryResponse updateTheory(String theoryId, UpdateTheoryRequest request);
    void deleteTheory(String theoryId);
}
