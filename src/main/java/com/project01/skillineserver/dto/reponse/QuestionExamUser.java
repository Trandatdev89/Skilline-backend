package com.project01.skillineserver.dto.reponse;

import com.project01.skillineserver.enums.QuestionType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionExamUser {
    private Long questionId;
    private String content;
    private QuestionType type;
    private Double maxScore;
    private Double scoreAchieved;
    private List<AnswerRes> answers;
}
