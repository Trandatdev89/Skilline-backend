package com.project01.skillineserver.dto.request;

public record QuizReq(Long id, String lectureId, String title, String desc
        , Integer timeLimit, Integer maxAttempt) {
}
