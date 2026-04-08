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

    public CourseResponse toCourseResponse(CourseEntity courseEntity, String thumbnailUrl) {

        return CourseResponse.builder()
                .id(courseEntity.getId())
                .title(courseEntity.getTitle())
                .thumbnail_url(thumbnailUrl)
                .level(courseEntity.getLevel())
                .priceOriginal(courseEntity.getPriceOriginal())
                .description(courseEntity.getDescription())
                .rate(courseEntity.getRate())
                .discount(courseEntity.getDiscount())
                .priceDiscount(courseEntity.getPriceDiscount())
                .isDelete(courseEntity.isDelete())
                .categoryId(courseEntity.getCategoryId())
                .publishStatus(courseEntity.getPublishStatus())
                .createdAt(dateUtil.format(courseEntity.getCreatedAt()))
                .updatedAt(dateUtil.format(courseEntity.getUpdatedAt()))
                .createdBy(courseEntity.getCreatedBy())
                .updatedBy(courseEntity.getUpdatedBy())
                .build();
    }
}
