package com.project01.skillineserver.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project01.skillineserver.entity.MediaAssetEntity;
import com.project01.skillineserver.entity.MediaAssetVariantEntity;
import com.project01.skillineserver.entity.MediaJobEntity;
import com.project01.skillineserver.enums.*;
import com.project01.skillineserver.kafka.event.MediaUploadedEvent;
import com.project01.skillineserver.repository.MediaAssetRepository;
import com.project01.skillineserver.repository.MediaAssetVariantRepository;
import com.project01.skillineserver.repository.MediaJobRepository;
import com.project01.skillineserver.service.Impl.HlsTranscodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Consumer lắng nghe event "media.uploaded" từ Kafka.
 * <p>
 * Luồng xử lý:
 * 1. Nhận event khi frontend upload xong lên S3
 * 2. Tạo MediaJob record để tracking
 * 3. Với VIDEO: trigger transcode HLS (FFmpeg hoặc AWS MediaConvert)
 * 4. Với IMAGE: resize + tạo variants (thumbnail, medium, full)
 * 5. Cập nhật processStatus trên MediaAsset
 * <p>
 * NOTE: Trong production thực tế, bước transcode video nặng nên
 * delegate sang AWS Elemental MediaConvert hoặc một worker riêng.
 * Code dưới đây mô phỏng flow hoàn chỉnh với FFmpeg local.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaProcessingConsumer {

    private final MediaAssetRepository mediaAssetRepository;
    private final MediaAssetVariantRepository variantRepository;
    private final MediaJobRepository mediaJobRepository;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final HlsTranscodeService hlsTranscodeService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @KafkaListener(
            topics = "${app.kafka.media-uploaded-topic:media.uploaded}",
            groupId = "media-processing-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleMediaUploaded(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        log.info("[Kafka] Received media.uploaded | assetId={}", record.key());

        MediaUploadedEvent event;
        try {
            event = objectMapper.convertValue(record.value(), MediaUploadedEvent.class);
        } catch (Exception e) {
            log.error("Cannot deserialize event key={}: {}", record.key(), e.getMessage());
            ack.acknowledge();
            return;
        }

        MediaAssetEntity asset = mediaAssetRepository.findById(event.getAssetId()).orElse(null);
        if (asset == null) {
            log.error("Asset not found: {}", event.getAssetId());
            ack.acknowledge();
            return;
        }

        MediaJobEntity job = MediaJobEntity.builder()
                .assetId(asset.getId())
                .jobType(asset.getAssetType() == AssetType.VIDEO
                        ? MediaJobType.TRANSCODE_HLS
                        : MediaJobType.GENERATE_THUMBNAIL)
                .status(JobStatus.RUNNING)
                .retryCount(0)
                .startedAt(Instant.now())
                .build();
        mediaJobRepository.save(job);

        asset.setProcessStatus(ProcessStatus.PROCESSING);
        mediaAssetRepository.save(asset);

        try {
            if (asset.getAssetType() == AssetType.VIDEO) {
                hlsTranscodeService.transcode(asset);
            } else {
                asset.setProcessStatus(ProcessStatus.COMPLETED);
                asset.setPlaybackType(PlaybackType.DIRECT);
                mediaAssetRepository.save(asset);
            }

            job.setStatus(JobStatus.COMPLETED);
            job.setFinishedAt(Instant.now());
            mediaJobRepository.save(job);
            log.info("[Kafka] Done asset [{}]", asset.getId());

        } catch (Exception e) {
            log.error("[Kafka] Failed asset [{}]: {}", asset.getId(), e.getMessage(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setFinishedAt(Instant.now());
            mediaJobRepository.save(job);
            asset.setProcessStatus(ProcessStatus.FAILED);
            asset.setErrorMessage(e.getMessage());
            mediaAssetRepository.save(asset);
        }

        ack.acknowledge();
    }

    // ==================== VIDEO PROCESSING ====================

    /**
     * Xử lý video: transcode sang HLS với nhiều resolution (360p, 480p, 720p, 1080p).
     * <p>
     * Flow thực tế với FFmpeg:
     * 1. Download video từ S3 về /tmp
     * 2. Chạy FFmpeg để tạo HLS segments: .ts files + .m3u8 playlists
     * 3. Upload toàn bộ HLS output lên S3 vào thư mục videos/hls/{uuid}/
     * 4. Tạo MediaAssetVariant records cho từng resolution
     * 5. Cập nhật MediaAsset với hlsMasterKey
     * <p>
     * NOTE: Với video lớn (>500MB), nên dùng AWS Elemental MediaConvert
     * thay vì FFmpeg trên application server.
     */
    private void processVideo(MediaUploadedEvent event, MediaJobEntity job) {
        log.info("Processing video asset [{}]", event.getAssetId());

        MediaAssetEntity asset = mediaAssetRepository.findById(event.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found: " + event.getAssetId()));

        // Cập nhật status → PROCESSING
        asset.setProcessStatus(ProcessStatus.PROCESSING);
        mediaAssetRepository.save(asset);

        // Xây dựng đường dẫn HLS output trên S3
        // videos/raw/2025/06/uuid.mp4 → videos/hls/2025/06/uuid/
        String rawKey = event.getObjectKey();
        String hlsBasePath = rawKey
                .replace("videos/raw/", "videos/hls/")
                .replaceAll("\\.[^.]+$", ""); // bỏ extension

        String hlsMasterKey = hlsBasePath + "/master.m3u8";

        /*
         * === ĐÂY LÀ NƠI TÍCH HỢP FFMPEG hoặc AWS MEDIACONVERT ===
         *
         * Option A - FFmpeg local (dev/small scale):
         * -----------------------------------------------
         * String localInput = downloadFromS3ToTemp(event.getBucket(), event.getObjectKey());
         * String localOutput = "/tmp/hls/" + assetId + "/";
         * runFFmpegHLS(localInput, localOutput);
         * uploadHlsDirectoryToS3(localOutput, hlsBasePath);
         *
         * Lệnh FFmpeg tạo adaptive HLS:
         * ffmpeg -i input.mp4 \
         *   -filter_complex "[v]split=3[v1][v2][v3]" \
         *   -map "[v1]" -s 640x360  -b:v 800k  -hls_segment_filename hls/360p_%03d.ts  hls/360p.m3u8 \
         *   -map "[v2]" -s 854x480  -b:v 1400k -hls_segment_filename hls/480p_%03d.ts  hls/480p.m3u8 \
         *   -map "[v3]" -s 1280x720 -b:v 2800k -hls_segment_filename hls/720p_%03d.ts  hls/720p.m3u8
         *
         * Option B - AWS MediaConvert (production):
         * -----------------------------------------------
         * mediaConvertClient.createJob(CreateJobRequest.builder()
         *     .role(mediaConvertRoleArn)
         *     .settings(jobSettings) // chỉ định input S3 URI + output HLS preset
         *     .build());
         * → job chạy async, AWS gửi SNS notification khi xong
         * → SNS trigger Lambda hoặc API endpoint để cập nhật DB
         */

        // Simulate: tạo variant records (trong thực tế tạo sau khi FFmpeg/MediaConvert xong)
        List<MediaAssetVariantEntity> variants = createHlsVariants(
                event.getAssetId(), hlsBasePath);
        variantRepository.saveAll(variants);

        // Cập nhật asset với HLS master key và thông tin
        asset.setProcessStatus(ProcessStatus.COMPLETED);
        asset.setHlsMasterKey(hlsMasterKey);
        asset.setPlaybackType(PlaybackType.HLS);
        mediaAssetRepository.save(asset);

        log.info("Video processed. HLS master: [{}]", hlsMasterKey);
    }

    private List<MediaAssetVariantEntity> createHlsVariants(String assetId, String hlsBasePath) {
        List<MediaAssetVariantEntity> variants = new ArrayList<>();

        record VariantConfig(VariantType type, int width, int height, long bitrate, int order) {
        }

        List<VariantConfig> configs = List.of(
                new VariantConfig(VariantType.HLS_360P, 640, 360, 800_000L, 1),
                new VariantConfig(VariantType.HLS_480P, 854, 480, 1_400_000L, 2),
                new VariantConfig(VariantType.HLS_720P, 1280, 720, 2_800_000L, 3),
                new VariantConfig(VariantType.HLS_1080P, 1920, 1080, 5_000_000L, 4)
        );

        for (VariantConfig cfg : configs) {
            String variantKey = hlsBasePath + "/" + cfg.type().name().toLowerCase() + ".m3u8";
            variants.add(MediaAssetVariantEntity.builder()
                    .assetId(assetId)
                    .variantType(cfg.type())
                    .bucket(bucket)
                    .objectKey(variantKey)
                    .mimeType("application/x-mpegURL")
                    .widthPx(cfg.width())
                    .heightPx(cfg.height())
                    .bitrate(cfg.bitrate())
                    .sortOrder(cfg.order())
                    .build());
        }

        return variants;
    }

    // ==================== IMAGE PROCESSING ====================

    /**
     * Xử lý ảnh: resize về các kích thước chuẩn, lưu variant lên S3.
     * Sử dụng AWS S3 Object Lambda hoặc CloudFront + Lambda@Edge để resize on-the-fly
     * là cách tốt hơn trong production. Đây là cách đơn giản nhất.
     */
    private void processImage(MediaUploadedEvent event, MediaJobEntity job) {
        log.info("Processing image asset [{}]", event.getAssetId());

        MediaAssetEntity asset = mediaAssetRepository.findById(event.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found: " + event.getAssetId()));

        /*
         * === ĐÂY LÀ NƠI TÍCH HỢP IMAGE PROCESSING ===
         *
         * Option A - Java (Thumbnailator / ImageIO):
         * -----------------------------------------------
         * byte[] original = downloadFromS3(event.getBucket(), event.getObjectKey());
         * byte[] thumbnail = Thumbnails.of(new ByteArrayInputStream(original))
         *     .size(300, 300).outputFormat("webp").toByteArray();
         * uploadToS3(thumbnailKey, thumbnail, "image/webp");
         *
         * Option B - CloudFront + Lambda@Edge (production, zero-cost per resize):
         * -----------------------------------------------
         * Không cần xử lý gì ở đây. CloudFront tự resize khi có request
         * với query param: ?width=300&height=300&format=webp
         */

        // Ảnh đơn giản → đánh dấu COMPLETED ngay
        asset.setProcessStatus(ProcessStatus.COMPLETED);
        asset.setPlaybackType(PlaybackType.DIRECT);
        mediaAssetRepository.save(asset);

        log.info("Image asset [{}] marked as COMPLETED", event.getAssetId());
    }
}