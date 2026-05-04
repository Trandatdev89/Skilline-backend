package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.projection.QuizAttemptProjection;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.AttemptQuizReq;

public interface QuizAttemptService {
    void save(AttemptQuizReq attemptQuizReq, Long userId);
    PageResponse<QuizAttemptProjection> getQuizAttempts(int page, int size, String sort, String keyword);
}
