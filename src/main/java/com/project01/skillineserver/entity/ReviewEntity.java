package com.project01.skillineserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "review")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ReviewEntity extends BaseEntity<Long> {

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String comment;   // da duoc lam sach bang XssSanitizer.cleanText() o ReviewServiceImpl

    private Integer rating;
}