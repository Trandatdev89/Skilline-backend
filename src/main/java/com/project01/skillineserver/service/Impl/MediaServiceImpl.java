package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.InitUploadResponse;
import com.project01.skillineserver.dto.request.InitUploadRequest;
import com.project01.skillineserver.entity.MediaAssetEntity;
import com.project01.skillineserver.enums.AssetType;
import com.project01.skillineserver.enums.PlaybackType;
import com.project01.skillineserver.enums.ProcessStatus;
import com.project01.skillineserver.enums.UploadStatus;
import com.project01.skillineserver.properties.KafkaTopicProperties;
import com.project01.skillineserver.repository.LectureRepository;
import com.project01.skillineserver.repository.MediaAssetRepository;
import com.project01.skillineserver.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaAssetRepository mediaAssetRepository;
    private final LectureRepository lectureRepository;
    private final com.project01.skillineserver.config.properties.StorageProperties storageProperties;
    private final KafkaTopicProperties kafkaTopicProperties;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Override
    public InitUploadResponse initUploadFile(InitUploadRequest initUploadRequest) {

        String uploadKey = UUID.randomUUID().toString();
        String extension = extractExtension(initUploadRequest.getFileName());

        String objectKey = buildObjectKey(initUploadRequest, uploadKey, extension);
        MediaAssetEntity assetEntity = MediaAssetEntity.builder()
                .assetType(initUploadRequest.getAssetType())
                .bucket(storageProperties.getPrivateBucket())
                .objectKey(objectKey)
                .originalFileName(initUploadRequest.getFileName())
                .mimeType(initUploadRequest.getMimeType())
                .sizeBytes(initUploadRequest.getSizeBytes())
                .playbackType(initUploadRequest.getAssetType() == AssetType.VIDEO ? PlaybackType.HLS : PlaybackType.FILE)
                .uploadStatus(UploadStatus.INITIATED)
                .processStatus(ProcessStatus.PENDING)
                .isPrivate(Boolean.TRUE.equals(initUploadRequest.getIsPrivate()))
                .build();

        mediaAssetRepository.save(assetEntity);

//        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                .bucket()
//                .build();

        return null;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    }

    private String buildObjectKey(InitUploadRequest request, String assetId, String extension) {
        String folder = switch (request.getAssetType()) {
            case IMAGE -> "images";
            case VIDEO -> "videos";
            case DOCUMENT -> "documents";
            case SUBTITLE -> "subtitle";
        };

        return "courses/" + request.getCourseId() + "/lectures/" + request.getLectureId()
                + "/" + folder
                + "/" + assetId
                + "/source/original" + extension;
    }
}
