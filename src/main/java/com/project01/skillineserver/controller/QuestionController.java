package com.project01.skillineserver.controller;


import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.QuestionExamUser;
import com.project01.skillineserver.dto.request.SaveQuestionListReq;
import com.project01.skillineserver.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/question")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ApiResponse<?> save(@RequestBody SaveQuestionListReq saveQuestionListReq) {
        questionService.save(saveQuestionListReq);
        return ApiResponse.builder()
                .code(200)
                .message("Save question success!")
                .build();
    }

    @GetMapping(value = "/exam")
    public ApiResponse<List<QuestionExamUser>> exam(@RequestParam Long quizId) {
        return ApiResponse.<List<QuestionExamUser>>builder()
                .code(200)
                .data(questionService.exam(quizId))
                .message("Get question success!")
                .build();
    }

    @DeleteMapping(value = "/{ids}")
    public ApiResponse<?> delete(@PathVariable List<Long> ids) {
        questionService.delete(ids);
        return ApiResponse.builder()
                .code(200)
                .message("Delete question success!")
                .build();
    }
}
