package com.project01.skillineserver.mapper;

import com.project01.skillineserver.dto.projection.CategoryProjection;
import com.project01.skillineserver.dto.reponse.CategoryResponse;
import com.project01.skillineserver.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryMapper {


    @Value("${domain.server}")
    private String DOMAIN_SERVER;

    private final DateUtil dateUtil;

    public CategoryResponse toCategoriesResponse(CategoryProjection category) {

        String timeCreatedConvert = dateUtil.format(category.getCreatedAt());
        String timeUpdatedConvert = dateUtil.format(category.getUpdatedAt());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .urlThumbnail(DOMAIN_SERVER + category.getPath())
                .slug(category.getSlug())
                .isActive(category.getIsActive())
                .createdAt(timeCreatedConvert)
                .updatedAt(timeUpdatedConvert)
                .build();
    }

}
