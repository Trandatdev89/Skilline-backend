package com.project01.skillineserver.entity;


import com.project01.skillineserver.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "questions")
public class QuestionEntity extends BaseEntity<Long> {
    @Column(name = "quiz_id")
    private Long quizId;
    private String content;
    @Enumerated(EnumType.STRING)
    private QuestionType type;
    private Double score;
    private Integer position;
}
