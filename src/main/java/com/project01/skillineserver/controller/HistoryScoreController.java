package com.project01.skillineserver.controller;


import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.HistoryExamUser;
import com.project01.skillineserver.service.AttemptAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/history-score-user")
public class HistoryScoreController {

    private final AttemptAnswerService attemptAnswerService;

    @GetMapping(value = "/exam")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<HistoryExamUser> getHistoryScoreExamOfUser(@AuthenticationPrincipal CustomUserDetail customUserDetail,
                                                                  @RequestParam Long attemptQuizId){

        return ApiResponse.<HistoryExamUser>builder()
                .message("Get history exam user success!")
                .data(attemptAnswerService.getHistoryScoreExamOfUser(attemptQuizId))
                .code(200)
                .build();
    }
}
