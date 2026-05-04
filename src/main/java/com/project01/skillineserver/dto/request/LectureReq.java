package com.project01.skillineserver.dto.request;

import com.project01.skillineserver.enums.PublishStatus;

public record LectureReq(String id, String title, Long courseId, boolean previewable, Long durationSeconds,
                         PublishStatus publishStatus, String contentAssetId, String thumbnailAssetId) {
}
