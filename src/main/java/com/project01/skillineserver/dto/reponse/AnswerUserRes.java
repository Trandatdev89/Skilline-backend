package com.project01.skillineserver.dto.reponse;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerUserRes {
    private Long answerId;
    private String content;
    private boolean isCorrect;
    private boolean isUserSelected;
}
