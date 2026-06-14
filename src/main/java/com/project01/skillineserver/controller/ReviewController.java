package com.project01.skillineserver.controller;


import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.ReviewRes;
import com.project01.skillineserver.dto.request.ReviewReq;
import com.project01.skillineserver.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/review")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<?> createReview(@RequestBody ReviewReq reviewReq) {
        reviewService.createReview(reviewReq);
        return ApiResponse.builder()
                .message("Save success !")
                .code(200)
                .build();
    }

    @GetMapping
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<List<ReviewRes>> getReviewByCourseId(@RequestParam Long courseId) {
        List<ReviewRes> reviews = reviewService.getReviewByCourseId(courseId);
        return ApiResponse.<List<ReviewRes>>builder()
                .message("Get List reviews success !")
                .code(200)
                .data(reviews)
                .build();
    }
}
