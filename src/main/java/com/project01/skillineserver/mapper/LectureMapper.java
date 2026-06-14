package com.project01.skillineserver.mapper;

import com.project01.skillineserver.dto.reponse.LectureResponse;
import com.project01.skillineserver.entity.LectureEntity;
import com.project01.skillineserver.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureMapper {

    private final DateUtil dateUtil;

    @Value("${domain.server}")
    private String DOMAIN_SERVER;

    @Value("${upload.directory.image}")
    private String PATH;

    public LectureResponse toLectureResponse(LectureEntity lectureEntity) {

        return LectureResponse.builder()
                .id(lectureEntity.getId())
                .title(lectureEntity.getTitle())
                .urlThumbnail(DOMAIN_SERVER + PATH + "/" + lectureEntity.getImage())
                .urlVideo(DOMAIN_SERVER + lectureEntity.getFilePath())
                .position(lectureEntity.getPosition())
                .publishStatus(lectureEntity.getPublishStatus())
                .previewable(lectureEntity.isPreviewable())
                .durationSeconds(lectureEntity.getDurationSeconds())
                .isDeleted(lectureEntity.isDelete())
                .processStatus(lectureEntity.getProcessStatus())
                .createAt(dateUtil.format(lectureEntity.getCreatedAt()))
                .updateAt(dateUtil.format(lectureEntity.getUpdatedAt()))
                .build();
    }
}
