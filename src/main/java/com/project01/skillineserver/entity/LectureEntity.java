package com.project01.skillineserver.entity;

import com.project01.skillineserver.enums.ProcessStatus;
import com.project01.skillineserver.enums.PublishStatus;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "lecture")
public class LectureEntity extends UuidEntity<String> {

    private String title;
    private String image;
    private Integer position;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "file_path",nullable = false)
    private String filePath;

    @Column(name = "is_previewable", nullable = false)
    private boolean previewable = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "publish_status", nullable = false, length = 20)
    private PublishStatus publishStatus = PublishStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_status", nullable = false, length = 20)
    private ProcessStatus processStatus = ProcessStatus.PENDING;

    @Column(name = "is_deleted", nullable = false)
    private boolean delete = false;

}

