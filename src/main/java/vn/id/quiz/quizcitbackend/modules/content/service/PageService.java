package vn.id.quiz.quizcitbackend.modules.content.service;

import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page.HomeSummaryResponse;

public interface PageService {
    HomeSummaryResponse getHomeSummary(String userId);
}
