package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.entity.CourseProgressEntity;
import com.project01.skillineserver.service.CourseProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/progress")
@RequiredArgsConstructor
public class CourseProgressController {

    private final CourseProgressService courseProgressService;

    @GetMapping
    public ApiResponse<List<CourseProgressEntity>> checkProgressCourse(@AuthenticationPrincipal CustomUserDetail customUserDetail,
                                                                       @RequestParam Long enrollmentId) {
        return ApiResponse.<List<CourseProgressEntity>>builder()
                .code(200)
                .message("Success!")
                .data(courseProgressService.checkProgressCourseProgressProjection(customUserDetail.getUser().getId(), enrollmentId))
                .build();
    }

    @PostMapping
    public ApiResponse<?> saveProgress(@RequestParam String lectureId,
                                       @RequestParam Long enrollmentId) {

        courseProgressService.updateProgressCourse(enrollmentId, lectureId);

        return ApiResponse.builder()
                .code(200)
                .message("update Success!")
                .build();
    }
}
