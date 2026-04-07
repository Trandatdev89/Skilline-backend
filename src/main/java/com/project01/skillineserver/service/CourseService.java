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

    void delete(List<String> courseId);
    CourseResponse getCourseById(Long id);
    void purchaseCourse(List<Long> idCourse,Long userId);

    PageResponse<CourseResponse> getCourses(int page, int size, String sort, String keyword, String categoryId);
    PageResponse<CourseResponse> searchAdvanceCourse(String[] search,int page,int size,String sort);

    PageResponse<CourseResponse> getCoursesWithCursor(Instant cursor, String sort, String keyword, int size, String categoryId);

    PageResponse<CourseResponse> getCoursesByMySelf(int page, int size, String sort, String keyword, String categoryId, Long userId);
}
