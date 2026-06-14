package com.project01.skillineserver.controller;

import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.LectureResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.LectureReq;
import com.project01.skillineserver.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/lecture")
public class LectureController {

    private final LectureService lectureService;

    @PostMapping(value = "/save")
    @PreAuthorize("@authorizationService.isAdmin()")
    public ApiResponse<?> save(@ModelAttribute LectureReq lectureReq) throws IOException, InterruptedException {
        lectureService.save(lectureReq);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<LectureResponse>> listLecture(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(required = false) String sort,
                                                                  @RequestParam(required = false) String keyword,
                                                                  @RequestParam Long courseId) {
        return ApiResponse.<PageResponse<LectureResponse>>builder()
                .data(lectureService.getListLecture(page, size, sort, keyword, courseId))
                .message("success!")
                .code(200)
                .build();
    }

    @DeleteMapping(value = "/{ids}")
    @PreAuthorize("@authorizationService.isAdmin()")
    public ApiResponse<?> delete(@PathVariable List<String> ids) {
        lectureService.delete(ids);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }
}
