package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.HistoryExamUser;


public interface AttemptAnswerService {
    HistoryExamUser getHistoryScoreExamOfUser(Long attemptQuizId);
}
