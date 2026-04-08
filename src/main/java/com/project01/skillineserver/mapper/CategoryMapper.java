package com.project01.skillineserver.mapper;

import com.project01.skillineserver.dto.reponse.CategoryResponse;
import com.project01.skillineserver.projection.CategoryProjection;
import com.project01.skillineserver.properties.CdnProperties;
import com.project01.skillineserver.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {


    private final DateUtil dateUtil;

    private final CdnProperties cdnProperties;

    public CategoryResponse toCategoriesResponse(CategoryProjection category) {

        String timeCreatedConvert = dateUtil.format(category.getCreatedAt());
        String timeUpdatedConvert = dateUtil.format(category.getUpdatedAt());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .urlThumbnail(cdnProperties.getDomain() + "/" + category.getObjectKey())
                .slug(category.getSlug())
                .isActive(category.isActive())
                .createdAt(timeCreatedConvert)
                .updatedAt(timeUpdatedConvert)
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdateBy())
                .build();
    }

}
