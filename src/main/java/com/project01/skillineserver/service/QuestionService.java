package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.QuestionExamUser;
import com.project01.skillineserver.dto.request.SaveQuestionListReq;

import java.util.List;

public interface QuestionService {
    void save(SaveQuestionListReq saveQuestionListReq);

    List<QuestionExamUser> exam(Long quizId);

    void delete(List<Long> ids);
}
