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
@Table(name = "quiz")
public class QuizEntity extends BaseEntity<Long> {
    @Column(name = "lecture_id")
    private String lectureId;
    private String title;
    private String description;
    @Column(name = "time_limit")
    private Integer timeLimit;
    private Integer maxAttempt;
}
