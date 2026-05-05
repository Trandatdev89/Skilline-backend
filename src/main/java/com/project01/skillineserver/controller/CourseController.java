package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.CourseReq;
import com.project01.skillineserver.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/course")
public class CourseController {

    private final CourseService courseService;

    @PostMapping(value = "/save")
    @PreAuthorize("@authorizationService.isAdmin()")
    public ApiResponse<?> saveCourse(@RequestBody CourseReq courseReq) throws IOException {
        courseService.save(courseReq);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<CourseResponse>> getCourses(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(required = false) String sort,
                                                                @RequestParam(required = false) Long categoryId,
                                                                @RequestParam(required = false) String keyword) {
        return ApiResponse.<PageResponse<CourseResponse>>builder()
                .code(200)
                .message("Success")
                .data(courseService.getCourses(page, size, sort, keyword, categoryId))
                .build();
    }

    @GetMapping(value = "/my-self")
    public ApiResponse<PageResponse<CourseResponse>> getCoursesByMySelf(@AuthenticationPrincipal CustomUserDetail customUserDetail,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        @RequestParam(required = false) String sort,
                                                                        @RequestParam(required = false) Long categoryId,
                                                                        @RequestParam(required = false) String keyword) {
        Long userId = customUserDetail.getUser().getId();
        return ApiResponse.<PageResponse<CourseResponse>>builder()
                .code(200)
                .message("Success")
                .data(courseService.getCoursesByMySelf(page, size, sort, keyword, categoryId, userId))
                .build();
    }

    @GetMapping(value = "/get-with-cursor")
    public ApiResponse<PageResponse<CourseResponse>> getCoursesWithCursor(@RequestParam(required = false) Long cursor,
                                                                          @RequestParam(required = false) String sort,
                                                                          @RequestParam(required = false) Long categoryId,
                                                                          @RequestParam(required = false) String keyword,
                                                                          @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.<PageResponse<CourseResponse>>builder()
                .code(200)
                .message("Success")
                .data(courseService.getCoursesWithCursor(cursor, sort, keyword, size, categoryId))
                .build();
    }

    @DeleteMapping(value = "/{ids}")
    @PreAuthorize("@authorizationService.isAdmin()")
    public ApiResponse<?> deleteCourse(@PathVariable List<Long> ids) {
        courseService.delete(ids);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping(value = "/{id}")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable Long id) {
        return ApiResponse.<CourseResponse>builder()
                .code(200)
                .message("Success")
                .data(courseService.getCourseById(id))
                .build();
    }

    @GetMapping(value = "/search-advance")
    public ApiResponse<PageResponse<?>> searchAdvanceCourse(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size,
                                                            @RequestParam(defaultValue = "id,desc") String sort,
                                                            @RequestParam(required = false) String... search) {

        return ApiResponse.<PageResponse<?>>builder()
                .data(courseService.searchAdvanceCourse(search, page, size, sort))
                .code(200)
                .message("Search course success!")
                .build();
    }

}
