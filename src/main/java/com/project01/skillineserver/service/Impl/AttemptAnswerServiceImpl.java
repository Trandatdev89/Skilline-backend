package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.projection.AnswerUserChoiceProjection;
import com.project01.skillineserver.dto.reponse.AnswerRes;
import com.project01.skillineserver.dto.reponse.HistoryExamUser;
import com.project01.skillineserver.dto.reponse.QuestionExamUser;
import com.project01.skillineserver.entity.AnswerEntity;
import com.project01.skillineserver.entity.QuestionEntity;
import com.project01.skillineserver.entity.QuizAttemptEntity;
import com.project01.skillineserver.repository.AnswerRepository;
import com.project01.skillineserver.repository.HistoryScoreUserRepository;
import com.project01.skillineserver.repository.QuestionRepository;
import com.project01.skillineserver.repository.QuizAttemptRepository;
import com.project01.skillineserver.service.AttemptAnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptAnswerServiceImpl implements AttemptAnswerService {

    private final HistoryScoreUserRepository historyScoreUserRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Override
    public HistoryExamUser getHistoryScoreExamOfUser(Long attemptQuizId) {
        QuizAttemptEntity quizAttemptOfUser = quizAttemptRepository.findQuizAttemptOfUserById(attemptQuizId);

        //List Question:
        List<QuestionEntity> questions = questionRepository.findAllByQuizId(quizAttemptOfUser.getQuizId());
        Set<Long> questionIds = questions.stream().map(QuestionEntity::getId).collect(Collectors.toSet());

        //List answers of list questionId
        Map<Long,List<AnswerEntity>> answerGroupByQuestionId  =  answerRepository
                .findAllByQuestionIdIn(questionIds)
                .stream()
                .collect(Collectors.groupingBy(AnswerEntity::getQuestionId));

        //List HistoryUserChoice:
        Map<Long,List<AnswerUserChoiceProjection>> answerUserChoiceGroupByQuestionId  = historyScoreUserRepository
                .findByAttemptQuizIdAndQuestionIdIn(attemptQuizId)
                .stream()
                .collect(Collectors.groupingBy(AnswerUserChoiceProjection::getQuestionId));

        List<QuestionExamUser> questionExamUsers = questions.stream()
                .map(question -> buildQuestionExamUser(
                        question,
                        answerGroupByQuestionId.getOrDefault(question.getId(), Collections.emptyList()),
                        answerUserChoiceGroupByQuestionId.getOrDefault(question.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());


        return HistoryExamUser.builder()
                .submittedAt(quizAttemptOfUser.getSubmittedAt())
                .totalScore(quizAttemptOfUser.getTotalScore())
                .quizAttemptId(quizAttemptOfUser.getId())
                .attemptNo(quizAttemptOfUser.getAttemptNo())
                .questions(questionExamUsers)
                .build();
    }

    private QuestionExamUser buildQuestionExamUser(
            QuestionEntity question,
            List<AnswerEntity> answers,
            List<AnswerUserChoiceProjection> selectedAnswerOfUser) {

        double scoreAchieved = selectedAnswerOfUser.stream()
                .mapToDouble(choice -> choice.getScore() == null ? 0.0 : choice.getScore())
                .sum();

        // Set các answer user đã chọn
        Set<Long> selectedAnswerIds = selectedAnswerOfUser.stream()
                .map(AnswerUserChoiceProjection::getAnswerId)
                .collect(Collectors.toSet());

        // Build answer responses
        List<AnswerRes> answerUserRes = answers.stream()
                .map(answer -> AnswerRes.builder()
                        .answerId(answer.getId())
                        .isCorrect(answer.isCorrect())
                        .isUserSelected(selectedAnswerIds.contains(answer.getId()))
                        .content(answer.getContent())
                        .build())
                .collect(Collectors.toList());

        return QuestionExamUser.builder()
                .questionId(question.getId())
                .maxScore(question.getScore())
                .type(question.getType())
                .content(question.getContent())
                .scoreAchieved(scoreAchieved)
                .answers(answerUserRes)
                .build();
    }

}
