package com.project01.skillineserver.mapper;

import com.project01.skillineserver.dto.reponse.LectureResponse;
import com.project01.skillineserver.entity.LectureEntity;
import com.project01.skillineserver.projection.CourseProjection;
import com.project01.skillineserver.repository.LectureRepository;
import com.project01.skillineserver.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureMapper {

    private final DateUtil dateUtil;
    private final LectureRepository lectureRepository;

    public LectureResponse toLectureResponse(LectureEntity lectureEntity) {

        CourseProjection courseProjection = lectureRepository.getCourseWithCategory(lectureEntity.getCourseId());

        return LectureResponse.builder()
                .id(lectureEntity.getId())
                .title(lectureEntity.getTitle())
                .urlThumbnail(lectureEntity.getThumbnailAssetId())
                .urlVideo(lectureEntity.getContentAssetId())
                .position(lectureEntity.getPosition())
                .publishStatus(lectureEntity.getPublishStatus())
                .previewable(lectureEntity.isPreviewable())
                .durationSeconds(lectureEntity.getDurationSeconds())
                .createAt(dateUtil.format(lectureEntity.getCreatedAt()))
                .updateAt(dateUtil.format(lectureEntity.getUpdatedAt()))
                .build();
    }
}
