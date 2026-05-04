package com.project01.skillineserver.controller;

import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.QuizReq;
import com.project01.skillineserver.entity.QuizEntity;
import com.project01.skillineserver.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/quiz")
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ApiResponse<?> save(@RequestBody QuizReq quizReq) {
        quizService.save(quizReq);
        return ApiResponse.builder()
                .code(200)
                .message("Save quiz success!")
                .build();
    }

    @DeleteMapping(value = "/{quizIds}")
    public ApiResponse<?> delete(@PathVariable List<Long> quizIds) {
        quizService.delete(quizIds);
        return ApiResponse.builder()
                .code(200)
                .message("Delete quiz success!")
                .build();
    }

    @GetMapping(value = "/get-by-course")
    public ApiResponse<PageResponse<QuizEntity>> getQuizByLectureId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long courseId) {
        return ApiResponse.<PageResponse<QuizEntity>>builder()
                .code(200)
                .data(quizService.getQuizByLectureId(page, size, sort, keyword, courseId))
                .message("Get quiz success!")
                .build();
    }

}
