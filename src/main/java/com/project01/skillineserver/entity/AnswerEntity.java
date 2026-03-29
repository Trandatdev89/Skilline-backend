package com.project01.skillineserver.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "answer") //dap an chuan cua teacher
public class AnswerEntity extends BaseEntity<Long> {
    @Column(name = "question_id")
    private Long questionId;
    private String content;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;
}
