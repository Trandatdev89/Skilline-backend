package com.project01.skillineserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "enrollment")
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    @Column(name = "progress_percent", nullable = false)
    private Integer progressPercent = 0;

    @Column(name = "time_expire", nullable = false)
    private Instant timeExpire;
}
