package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.entity.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    @Query(value = "SELECT co " +
            "FROM UserEntity us " +
            "INNER JOIN EnrollmentEntity en ON us.id = en.userId " +
            "INNER JOIN CourseEntity co ON co.id = en.courseId " +
            "WHERE us.id = :userId " +
            "AND (en.timeExpire IS NULL OR en.timeExpire > NOW())")
    List<CourseEntity> getListCourseUserBought(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) > 0 " +
            "FROM enrollment en " +
            "WHERE en.user_id = :userId " +
            "AND en.course_id in :courseId " +
            "AND (en.time_expire IS NULL OR en.time_expire > NOW())",
            nativeQuery = true)
    int isUserEnrolledInCourse(@Param("userId") Long userId, @Param("courseId") List<Long> courseId);
}
