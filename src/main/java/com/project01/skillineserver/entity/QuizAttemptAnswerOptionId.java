package com.project01.skillineserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptAnswerOptionId implements Serializable {

    @Column(name = "history_answer_user_id")
    private Long historyAnswerUserId;

    @Column(name = "answer_id")
    private Long answerId;
}
