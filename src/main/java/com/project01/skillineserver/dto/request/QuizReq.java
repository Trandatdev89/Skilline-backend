package com.project01.skillineserver.dto.request;

import com.project01.skillineserver.enums.ExpireUnit;

public record QuizReq(Long id, String lectureId, String title, String desc
        , Integer timeLimit, Integer maxAttempt, ExpireUnit timeUnit) {
}
