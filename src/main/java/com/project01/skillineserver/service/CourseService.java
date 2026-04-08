package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.CourseReq;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.entity.UserEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CourseService {
    CourseEntity save(CourseReq courseReq) throws IOException;
    void delete(List<Long> courseId);
    CourseResponse getCourseById(Long id);
    void purchaseCourse(List<Long> idCourse,Long userId);
    List<CourseResponse> getListCourseById(List<Long> ids);
    PageResponse<CourseResponse> getCourses(int page, int size, String sort, String keyword,Long categoryId);
    PageResponse<CourseResponse> searchAdvanceCourse(String[] search,int page,int size,String sort);
    PageResponse<CourseResponse> getCoursesWithCursor(LocalDateTime cursor,String sort,String keyword,int size);
    PageResponse<CourseResponse> getCoursesByMySelf(int page, int size, String sort, String keyword,Long categoryId,Long userId);
}
