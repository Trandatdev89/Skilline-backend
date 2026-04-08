package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.CourseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<CourseEntity,Long>, JpaSpecificationExecutor<CourseEntity> {
    List<CourseEntity> findAllByCategoryId(Long categoryId);

    @Query("select c from CourseEntity c where c.isDelete = false and c.id in :id")
    List<CourseEntity> findAllByCourseIdIn(List<Long> id);

    @Query("select c from CourseEntity c where c.isDelete = false and c.id = :id")
    Optional<CourseEntity> findByCourseId(Long id);

    @Modifying
    @Query("delete from CourseEntity c where c.categoryId in :categoryIds")
    void deleteAllByCategoryIdIn(List<Long> categoryIds);

    @Query("select c " +
            "from CourseEntity c " +
            "where c.isDelete = false and (?1 is null or c.title like lower(concat('%',?1,'%'))) " +
            "and (?2 is null or c.categoryId = ?2 )")
    Page<CourseEntity> getCourses(String title,Long category_id, Pageable pageable);

    @Query("select c " +
            "from CourseEntity c " +
            "where c.isDelete = false and c.createdBy=?3 and(?1 is null or c.title like lower(concat('%',?1,'%'))) " +
            "and (?2 is null or c.categoryId = ?2 )")
    Page<CourseEntity> getCoursesByMySelf(String title,Long category_id,Long userId,Pageable pageable);

    @Query("SELECT c.id FROM CourseEntity c WHERE c.id IN :ids")
    List<Long> findAllIdsByIdIn(@Param("ids") List<Long> ids);

}
