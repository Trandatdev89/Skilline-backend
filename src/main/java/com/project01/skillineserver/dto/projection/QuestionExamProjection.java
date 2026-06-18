package com.project01.skillineserver.dto.projection;

import com.project01.skillineserver.enums.QuestionType;

public interface QuestionExamProjection {
    Long getQuestionId();
    String getQuestionContent();
    Double getScore();
    QuestionType getType();
    Long getAnswerId();
    String getAnswerContent();
    Boolean getIsCorrect();
}
