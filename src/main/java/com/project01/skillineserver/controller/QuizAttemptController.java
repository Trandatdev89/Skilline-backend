package com.project01.skillineserver.controller;


import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.projection.QuizAttemptProjection;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.AttemptQuizReq;
import com.project01.skillineserver.service.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/attempt-quiz")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping
    public ApiResponse<?> save(@RequestBody AttemptQuizReq attemptQuizReq,
                               @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        quizAttemptService.save(attemptQuizReq, customUserDetail.getUser().getId());
        return ApiResponse.builder()
                .code(200)
                .message("Save exam quiz success!")
                .build();
    }

    @GetMapping(value = "/personal")
    public ApiResponse<PageResponse<QuizAttemptProjection>> getQuizAttempts(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @RequestParam(required = false) String sort,
                                                                            @RequestParam(required = false) String keyword) {
        return ApiResponse.<PageResponse<QuizAttemptProjection>>builder()
                .data(quizAttemptService.getQuizAttempts(page, size, sort, keyword))
                .code(200)
                .message("Get list exam quiz success!")
                .build();
    }

}
