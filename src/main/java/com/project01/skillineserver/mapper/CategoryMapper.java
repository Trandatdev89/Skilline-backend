package com.project01.skillineserver.mapper;

import com.project01.skillineserver.dto.reponse.CategoryResponse;
import com.project01.skillineserver.entity.CategoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    public CategoryResponse toCategoriesResponse(CategoryEntity category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .urlThumbnail(category.getThumbnailAssetId())
                .isActive(category.isActive())
                .build();
    }
}
