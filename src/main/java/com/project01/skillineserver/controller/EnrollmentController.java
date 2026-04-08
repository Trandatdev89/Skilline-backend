package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.projection.CourseProjection;
import com.project01.skillineserver.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping(value = "/buy")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<List<CourseProjection>> getListCourseUserBuy(@AuthenticationPrincipal CustomUserDetail customUserDetail){
        return ApiResponse.<List<CourseProjection>>builder()
                .data(enrollmentService.getListCourseUserBuy(customUserDetail.getUser().getId()))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping(value = "/check")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<Boolean> checkUserEnrollment(@RequestParam Long courseId){
        return ApiResponse.<Boolean>builder()
                .code(200)
                .message("success")
                .data(enrollmentService.checkUserEnrollment(courseId))
                .build();
    }
}
