package com.project01.skillineserver.service;

import com.project01.skillineserver.entity.CourseProgressEntity;

import java.util.List;

public interface CourseProgressService {
    void updateProgressCourse(Long enrollmentId,String lectureId);
    List<CourseProgressEntity> checkProgressCourseProgressProjection(Long userId, Long enrollment);
}
