package com.project01.skillineserver.dto.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnswerRes {
    private Long answerId;
    private String content;
    private Boolean  isCorrect;
    private Boolean  isUserSelected;
}
