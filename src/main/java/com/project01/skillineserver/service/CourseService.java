package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.CourseReq;
import com.project01.skillineserver.entity.CourseEntity;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface CourseService {
    CourseEntity save(CourseReq courseReq) throws IOException;

    void delete(List<Long> courseId);

    CourseResponse getCourseById(Long id);

    PageResponse<CourseResponse> getCourses(int page, int size, String sort, String keyword, Long categoryId);

    PageResponse<CourseResponse> searchAdvanceCourse(String[] search, int page, int size, String sort);

    PageResponse<CourseResponse> getCoursesWithCursor(Instant cursor, String sort, String keyword, int size, Long categoryId);

    PageResponse<CourseResponse> getCoursesByMySelf(int page, int size, String sort, String keyword, Long categoryId, Long userId);
}
