package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.LectureEntity;
import com.project01.skillineserver.projection.CourseProjection;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LectureRepository extends JpaRepository<LectureEntity, String> {
    Page<LectureEntity> findAllByCourseId(PageRequest pageRequest, Long courseId);

    @Query("SELECT co.title as title, " +
            "cat.name as categoryName " +
            "FROM CourseEntity co " +
            "LEFT JOIN CategoryEntity cat on co.categoryId=cat.id " +
            "WHERE co.id = :courseId")
    CourseProjection getCourseWithCategory(@Param("courseId") Long courseId);


    List<LectureEntity> findAllByCourseId(Long courseId);

    @Query("select count(le.id) from LectureEntity le inner join CourseEntity co on le.courseId=co.id where le.courseId = :courseId")
    Long countLectureByCourseId(Long courseId);


    @Query("select le from LectureEntity le " +
            "inner join CourseEntity co on co.id=le.courseId " +
            "where le.courseId=?2 and co.isDelete = false and" +
            "(?1 is null or le.title like lower(concat('%',?1,'%')))")
    Page<LectureEntity> getLectures(String keyword, Long courseId, PageRequest pageRequest);


    @Query(value = """
            select max(le.position) as max_position from lecture le where le.course_id = :courseId
            """,nativeQuery = true)
    Integer findMaxPosition(Long courseId);
}
