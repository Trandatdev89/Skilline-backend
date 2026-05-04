package com.project01.skillineserver.dto.projection;

import java.time.Instant;

public interface QuizAttemptProjection {
    Long getUserId();
    String getFullName();
    String getTitle();
    String getQuizId();
    Double getTotalScore();
    Instant getSubmittedAt();
    Integer getAttemptNo();

}
