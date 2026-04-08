package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.CourseReq;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.enums.LevelEnum;
import com.project01.skillineserver.repository.CourseRepository;
import com.project01.skillineserver.service.CourseService;
import com.project01.skillineserver.specification.CourseSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/course")
public class CourseController {

    private final CourseService courseService;
    private final CourseSpecifications courseSpecifications;
    private final CourseRepository courseRepository;

    @PostMapping(value = "/save")
    @PreAuthorize("@authorizationService.isAdmin()")
    public ApiResponse<CourseEntity> saveCourse(@ModelAttribute CourseReq courseReq) throws IOException {
        return ApiResponse.<CourseEntity>builder()
                .code(200)
                .message("Success")
                .data(courseService.save(courseReq))
                .build();
    }

    @GetMapping(value = "/all")
    public ApiResponse<PageResponse<CourseResponse>> getCourses(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(required = false) String sort,
                                                                @RequestParam(required = false) Long categoryId,
                                                                @RequestParam(required = false) String keyword) {
        return ApiResponse.<PageResponse<CourseResponse>>builder()
                .code(200)
                .message("Success")
                .data(courseService.getCourses(page, size, sort, keyword,categoryId))
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
                .data(courseService.getCoursesByMySelf(page, size, sort, keyword,categoryId,userId))
                .build();
    }

    @GetMapping(value = "/cursor")
    public ApiResponse<PageResponse<CourseResponse>> getCoursesWithCursor(@RequestParam LocalDateTime cursor, @RequestParam(required = false) String sort,
                                                                          @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "20") int size
                                                                          ){
        return ApiResponse.<PageResponse<CourseResponse>>builder()
                .code(200)
                .message("Success")
                .data(courseService.getCoursesWithCursor(cursor, sort, keyword,size))
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

    @GetMapping(value = "/list/{ids}")
    public ApiResponse<?> getListCourseById(@PathVariable List<Long> ids) {
        return ApiResponse.builder()
                .code(200)
                .data(courseService.getListCourseById(ids))
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

    @GetMapping(value = "/search-with-specification")
    public ApiResponse<?> searchWithSpecification(@RequestParam Map<String, Object> params) {
        Specification<CourseEntity> spec = Specification.where(null);

        for (Map.Entry<String, Object> item : params.entrySet()) {
            String key = item.getKey();
            Object value = item.getValue();
            if (key.equals("categoryId")) {
                spec = spec.and(courseSpecifications.hasCategoryId(Long.parseLong(value.toString())));
            } else if (key.contains("Start") || key.contains("End")) {
                spec = spec.and(courseSpecifications.hasFieldRange(key, value));
            } else {
                spec = spec.and(courseSpecifications.hasField(key, value));
            }
        }

        List<CourseEntity> list = courseRepository.findAll(spec);

        return ApiResponse.builder()
                .data(list)
                .code(200)
                .message("Search course success!")
                .build();
    }

}
