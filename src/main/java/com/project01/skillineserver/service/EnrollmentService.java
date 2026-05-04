package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.CourseResponse;

import java.util.List;

public interface EnrollmentService {
    List<CourseResponse> getListCourseUserBought(Long userId);

    Boolean checkUserEnrollment(List<Long> courseId, Long userId);

    void saveEnrollment(List<Long> courseIds, Long userId);
}
