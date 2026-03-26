package com.project01.skillineserver.entity;

import com.project01.skillineserver.enums.AssetType;
import com.project01.skillineserver.enums.PlaybackType;
import com.project01.skillineserver.enums.ProcessStatus;
import com.project01.skillineserver.enums.UploadStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(
        name = "media_asset",
        indexes = {
                @Index(name = "idx_media_asset_asset_type", columnList = "asset_type"),
                @Index(name = "idx_media_asset_upload_status", columnList = "upload_status"),
                @Index(name = "idx_media_asset_process_status", columnList = "process_status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_media_asset_object_key", columnNames = "object_key")
        }
)
public class MediaAssetEntity extends UuidEntity<String> {

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 30)
    private AssetType assetType;

    @Column(name = "bucket", nullable = false, length = 255)
    private String bucket;

    @Column(name = "object_key", nullable = false, length = 1024)
    private String objectKey;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "mime_type", length = 150)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "etag", length = 255)
    private String etag;

    @Column(name = "checksum_sha256", length = 255)
    private String checksumSha256;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "playback_type", length = 20)
    private PlaybackType playbackType;

    @Column(name = "hls_master_key", length = 1024)
    private String hlsMasterKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 20)
    private UploadStatus uploadStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_status", nullable = false, length = 20)
    private ProcessStatus processStatus;

    @Column(name = "transcode_job_id", length = 255)
    private String transcodeJobId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = true;
}