package com.project01.skillineserver.service;

import com.project01.skillineserver.entity.EnrollmentEntity;
import com.project01.skillineserver.projection.CourseProjection;

import java.util.List;

public interface EnrollmentService {
    List<CourseProjection> getListCourseUserBought(Long userId);

    Boolean checkUserEnrollment(Long courseId);

    void enrollmentCourses(List<EnrollmentEntity> enrollmentEntity);
}
