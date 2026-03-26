package com.project01.skillineserver.entity;

import com.project01.skillineserver.enums.JobStatus;
import com.project01.skillineserver.enums.MediaJobType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "media_job",
        indexes = {
                @Index(name = "idx_media_job_asset_id", columnList = "asset_id"),
                @Index(name = "idx_media_job_status", columnList = "status")
        })
public class MediaJobEntity extends UuidEntity<String> {

    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private MediaJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;
}