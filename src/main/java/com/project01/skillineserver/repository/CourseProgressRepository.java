package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.CourseProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseProgressRepository extends JpaRepository<CourseProgressEntity,Long> {

//    @Query("select cp.isCompleted,cp.completedAt from CourseProgressEntity cp " +
//            "inner join LectureEntity le on le.id = cp.enrollmentId")
//    CourseProgressProjection checkProgressCourseProgressProjection(Long userId,String lectureId);

    //lay tat ca progress cua user trong 1 course
    @Query("SELECT cp FROM CourseProgressEntity cp " +
            "INNER JOIN EnrollmentEntity en ON en.id = cp.enrollmentId " +
            "WHERE en.userId = :userId AND en.courseId = :courseId")
    List<CourseProgressEntity> getProgressUserInOnCourse(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );
}
