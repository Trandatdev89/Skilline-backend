package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.CategoryEntity;
import com.project01.skillineserver.projection.CategoryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    @Modifying
    @Query("update CategoryEntity c set c.isActive = false where c.id in :ids")
    void deleteCategoryByIds(@Param("ids") List<Long> ids);

    @Query("select c.id," +
            "c.name," +
            "c.createdAt," +
            "c.updatedAt," +
            "c.createdBy," +
            "c.updatedBy," +
            "c.slug, " +
            "media.objectKey " +
            "from CategoryEntity c " +
            "left join MediaAssetEntity media on c.thumbnailAssetId = media.id " +
            "where c.isActive = true and (?1 is null or lower(c.name) like lower(concat('%',?1,'%')))")
    Page<CategoryProjection> getCategories(String keyword, Pageable pageable);

    @Query("select c.id," +
            "c.name," +
            "c.createdAt," +
            "c.updatedAt," +
            "c.createdBy," +
            "c.updatedBy," +
            "c.slug, " +
            "media.objectKey " +
            "from CategoryEntity c " +
            "left join MediaAssetEntity media on c.thumbnailAssetId = me.id " +
            "where c.isActive = true and c.createdBy = ?2 and (?1 is null or lower(c.name) like lower(concat('%',?1,'%')))")
    Page<CategoryProjection> getCategoriesMySelf(String keyword, Long userId, Pageable pageable);
}
