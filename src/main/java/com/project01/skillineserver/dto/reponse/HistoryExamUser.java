package com.project01.skillineserver.dto.reponse;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryExamUser {
    private Long quizAttemptId;
    private Instant submittedAt;
    private Double totalScore;
    private Integer attemptNo;
    private List<QuestionExamUser> questions;
}
