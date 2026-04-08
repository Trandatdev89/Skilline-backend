package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    void deleteByIdIn(List<Long> id);

    @Modifying
    @Query("update CategoryEntity c set c.isActive = false where c.id in :ids")
    void deleteCategoryByIds(@Param("ids") List<Long> ids);

    @Query("select c " +
            "from CategoryEntity c " +
            "where c.isActive = true and (?1 is null or lower(c.name) like lower(concat('%',?1,'%')))")
    Page<CategoryEntity> getCategories(String keyword, Pageable pageable);

    @Query("select c " +
            "from CategoryEntity c " +
            "where c.isActive = true and c.createdBy = ?2 and (?1 is null or lower(c.name) like lower(concat('%',?1,'%')))")
    Page<CategoryEntity> getCategoriesMySelf(String keyword, Long userId, Pageable pageable);
}
