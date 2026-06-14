package com.project01.skillineserver.entity;


import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "history_score_user")
public class AttemptAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_quiz_id")
    private Long attemptQuizId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "answerId")
    private Long answerId;    // FK -> answer_option

    private String answerText;

    private Double score;
}
