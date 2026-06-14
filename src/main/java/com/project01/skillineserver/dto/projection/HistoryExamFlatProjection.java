package com.project01.skillineserver.dto.projection;

import com.project01.skillineserver.enums.QuestionType;

import java.time.Instant;

public interface HistoryExamFlatProjection {
    Long getAttemptId();
    Instant getSubmittedAt();
    Double getTotalScore();

    Long getQuestionId();
    String getQuestionContent();
    QuestionType getQuestionType();
    Double getMaxScore();

    Long getAnswerId();
    String getAnswerContent();
    Boolean getIsCorrect();
    Long getIsUserSelected();

    Double getScoreAchieved();
}
