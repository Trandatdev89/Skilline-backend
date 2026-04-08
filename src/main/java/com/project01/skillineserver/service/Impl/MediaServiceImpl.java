package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.InitUploadResponse;
import com.project01.skillineserver.dto.reponse.MediaAssetResponse;
import com.project01.skillineserver.dto.request.ConfirmUploadRequest;
import com.project01.skillineserver.dto.request.InitUploadRequest;
import com.project01.skillineserver.entity.MediaAssetEntity;
import com.project01.skillineserver.enums.AssetType;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.ProcessStatus;
import com.project01.skillineserver.enums.UploadStatus;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.kafka.event.MediaUploadedEvent;
import com.project01.skillineserver.properties.CdnProperties;
import com.project01.skillineserver.properties.KafkaTopicProperties;
import com.project01.skillineserver.repository.MediaAssetRepository;
import com.project01.skillineserver.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final S3Presigner s3Presigner;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MediaAssetRepository mediaAssetRepository;
    private final CdnProperties cdnProperties;
    private final KafkaTopicProperties kafkaTopicProperties;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // Presigned URL hết hạn sau 15 phút - đủ thời gian để frontend upload
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(15);

    /**
     * Bước 1: Frontend gọi API này để lấy presigned URL.
     * Server tạo record trong DB (status=PENDING), rồi trả về URL để frontend
     * PUT file thẳng lên S3 mà không qua server (tránh server làm bottleneck).
     */
    @Override
    @Transactional
    public InitUploadResponse initUploadFile(InitUploadRequest req) {

        // Tạo object key theo cấu trúc: images/2025/06/uuid.jpg
        String objectKey = buildObjectKey(req.assetType(), req.originalFileName());

        // Tạo bản ghi trong DB trước - trạng thái PENDING
        MediaAssetEntity asset = MediaAssetEntity.builder()
                .assetType(req.assetType())
                .bucket(bucket)
                .objectKey(objectKey)
                .originalFileName(req.originalFileName())
                .mimeType(req.mimeType())
                .sizeBytes(req.sizeBytes())
                .uploadStatus(UploadStatus.PENDING)
                // Video cần transcode HLS → PENDING; ảnh không cần transcode → COMPLETED
                .processStatus(req.assetType() == AssetType.VIDEO
                        ? ProcessStatus.PENDING
                        : ProcessStatus.COMPLETED)
                .isPrivate(req.assetType() == AssetType.VIDEO) // video private, ảnh public
                .build();

        mediaAssetRepository.save(asset);
        log.info("Created media asset [{}] for objectKey [{}]", asset.getId(), objectKey);

        // Tạo presigned PUT URL - frontend sẽ dùng URL này để upload thẳng lên S3
        String uploadUrl = generatePresignedPutUrl(objectKey, req.mimeType());

        return InitUploadResponse.builder()
                .assetId(asset.getId())
                .uploadUrl(uploadUrl)
                .objectKey(objectKey)
                .build();
    }

    /**
     * Bước 2: Frontend gọi API này SAU KHI đã upload xong lên S3.
     * Server cập nhật status → UPLOADED, rồi publish Kafka event để
     * worker xử lý async (transcode HLS cho video, resize ảnh...).
     */
    @Override
    @Transactional
    public MediaAssetResponse confirmUpload(ConfirmUploadRequest req) {

        MediaAssetEntity asset = mediaAssetRepository.findById(req.assetId())
                .orElseThrow(() -> new AppException(ErrorCode.MEDIA_ASSET_NOT_FOUND));

        // Chỉ accept nếu đang ở trạng thái PENDING
        if (asset.getUploadStatus() != UploadStatus.PENDING) {
            throw new AppException(ErrorCode.MEDIA_ASSET_ALREADY_PROCESSED);
        }

        // Cập nhật status → UPLOADED
        asset.setUploadStatus(UploadStatus.UPLOADED);
        mediaAssetRepository.save(asset);

        // Publish Kafka event để xử lý async
        // Video → worker sẽ transcode HLS
        // Ảnh → worker có thể resize, tạo thumbnail
        MediaUploadedEvent event = MediaUploadedEvent.builder()
                .assetId(asset.getId())
                .assetType(asset.getAssetType())
                .bucket(asset.getBucket())
                .objectKey(asset.getObjectKey())
                .mimeType(asset.getMimeType())
                .sizeBytes(asset.getSizeBytes())
                .build();

        // Key = assetId để đảm bảo cùng 1 asset đi vào cùng 1 partition (ordered)
        kafkaTemplate.send(kafkaTopicProperties.getMediaUploadedTopic(), asset.getId(), event);
        log.info("Published media.uploaded event for asset [{}] type [{}]",
                asset.getId(), asset.getAssetType());

        return toResponse(asset);
    }

    /**
     * Lấy URL public để truy cập asset.
     * - Ảnh: CloudFront URL thẳng (public)
     * - Video: CloudFront URL của HLS master playlist (private, cần signed cookie)
     */
    @Override
    public MediaAssetResponse getAssetById(String assetId) {
        MediaAssetEntity asset = mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new AppException(ErrorCode.MEDIA_ASSET_NOT_FOUND));
        return toResponse(asset);
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Tạo object key có cấu trúc rõ ràng:
     * - images/2025/06/550e8400-e29b-41d4-a716-446655440000.jpg
     * - videos/raw/2025/06/550e8400-e29b-41d4-a716-446655440000.mp4
     */
    private String buildObjectKey(AssetType assetType, String originalFileName) {
        String ext = extractExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        String datePath = Instant.now().toString().substring(0, 7).replace("-", "/"); // "2025/06"

        return switch (assetType) {
            case IMAGE -> String.format("images/%s/%s.%s", datePath, uuid, ext);
            case VIDEO -> String.format("videos/raw/%s/%s.%s", datePath, uuid, ext);
        };
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "bin";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Tạo S3 Presigned PUT URL.
     * Frontend sẽ dùng URL này để PUT file thẳng vào S3 bằng HTTP PUT request.
     * Server không nhận file → không tốn bandwidth server.
     */
    private String generatePresignedPutUrl(String objectKey, String mimeType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(mimeType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        return presigned.url().toString();
    }

    /**
     * Xây dựng URL CloudFront để truy cập asset:
     * - Ảnh:  https://cdn.example.com/images/2025/06/uuid.jpg
     * - Video: https://cdn.example.com/videos/hls/2025/06/uuid/master.m3u8
     */
    private String buildCdnUrl(MediaAssetEntity asset) {
        String cdnDomain = cdnProperties.getDomain(); // ví dụ: https://d1234abcd.cloudfront.net

        if (asset.getAssetType() == AssetType.VIDEO) {
            // HLS master playlist
            if (asset.getHlsMasterKey() != null) {
                return cdnDomain + "/" + asset.getHlsMasterKey();
            }
            // Video chưa transcode xong
            return null;
        }

        // Ảnh: truy cập thẳng qua CloudFront
        return cdnDomain + "/" + asset.getObjectKey();
    }

    private MediaAssetResponse toResponse(MediaAssetEntity asset) {
        return MediaAssetResponse.builder()
                .assetId(asset.getId())
                .assetType(asset.getAssetType())
                .publicUrl(buildCdnUrl(asset))
                .mimeType(asset.getMimeType())
                .sizeBytes(asset.getSizeBytes())
                .widthPx(asset.getWidthPx())
                .heightPx(asset.getHeightPx())
                .durationSeconds(asset.getDurationSeconds())
                .playbackType(asset.getPlaybackType())
                .uploadStatus(asset.getUploadStatus())
                .processStatus(asset.getProcessStatus())
                .build();
    }
}