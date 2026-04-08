package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.entity.EnrollmentEntity;
import com.project01.skillineserver.projection.CourseProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity,Long> {


    @Query(value = "SELECT co.id as id, " +
            "co.title as title, " +
            "co.description as description, " +
            "co.thumbnail_url as thumbnailUrl, " +
            "co.level as level, " +
            "co.price as price, " +
            "co.rate as rate " +
            "FROM users us " +
            "INNER JOIN enrollment en ON us.id = en.user_id " +
            "INNER JOIN courses co ON co.id = en.course_id " +
            "WHERE us.id = :userId", nativeQuery = true)
    List<CourseProjection> getListCourseUserBuy(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) > 0 " +
            "FROM enrollment en " +
            "WHERE en.user_id = :userId AND en.course_id = :courseId",
            nativeQuery = true)
    int isUserEnrolledInCourse(@Param("userId") Long userId,@Param("courseId") Long courseId);
}
