package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.QuizReq;
import com.project01.skillineserver.entity.QuizEntity;

import java.util.List;

public interface QuizService {
    void save(QuizReq quizReq);
    void delete(List<Long> quizIds);

    PageResponse<QuizEntity> getQuizByLectureId(int page, int size, String sort, String keyword, Long courseId);
}
