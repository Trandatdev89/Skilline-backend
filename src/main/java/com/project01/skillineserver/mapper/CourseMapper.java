package com.project01.skillineserver.mapper;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    @Autowired
    private DateUtil dateUtil;

    public CourseResponse toLectureResponse(CourseEntity courseEntity) {

        return CourseResponse.builder()
                .id(courseEntity.getId())
                .title(courseEntity.getTitle())
                .thumbnail_url(courseEntity.getThumbnailAssetId())
                .level(courseEntity.getLevel())
                .price(courseEntity.getPrice())
                .description(courseEntity.getDescription())
                .rate(courseEntity.getRate())
                .discount(courseEntity.getDiscountPrice())
                .status(courseEntity.isDelete())
                .categoryId(courseEntity.getCategoryId())
                .publishStatus(courseEntity.getPublishStatus())
                .createAt(dateUtil.format(courseEntity.getCreatedAt()))
                .updateAt(dateUtil.format(courseEntity.getUpdatedAt()))
                .build();
    }
}
