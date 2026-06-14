package com.project01.skillineserver.entity;

import com.project01.skillineserver.enums.ExpireUnit;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "time_unit", length = 20)
    private ExpireUnit timeUnit;

    private Integer maxAttempt;
}
