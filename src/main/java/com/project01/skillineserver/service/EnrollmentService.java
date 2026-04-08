package com.project01.skillineserver.service;

import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.projection.CourseProjection;

import java.util.List;

public interface EnrollmentService {
    List<CourseProjection> getListCourseUserBuy(Long userId);
    Boolean checkUserEnrollment(Long courseId);
}
