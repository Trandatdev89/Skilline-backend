package com.project01.skillineserver.mapper;

import com.project01.skillineserver.dto.reponse.CategoryResponse;
import com.project01.skillineserver.projection.CategoryProjection;
import com.project01.skillineserver.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryMapper {

    private final DateUtil dateUtil;

    public CategoryResponse toCategoriesResponse(CategoryProjection category, String urlThumbnail) {

        String timeCreatedConvert = dateUtil.format(category.getCreatedAt());
        String timeUpdatedConvert = dateUtil.format(category.getUpdatedAt());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .urlThumbnail(urlThumbnail)
                .slug(category.getSlug())
                .isActive(category.getIsActive())
                .createdAt(timeCreatedConvert)
                .updatedAt(timeUpdatedConvert)
                .build();
    }

}
