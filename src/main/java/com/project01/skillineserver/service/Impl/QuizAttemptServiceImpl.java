package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.projection.QuizAttemptProjection;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.AnswerUserReq;
import com.project01.skillineserver.dto.request.AttemptQuizReq;
import com.project01.skillineserver.entity.AnswerEntity;
import com.project01.skillineserver.entity.AttemptAnswerEntity;
import com.project01.skillineserver.entity.QuizAttemptEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.HistoryScoreUserRepository;
import com.project01.skillineserver.repository.QuizAttemptRepository;
import com.project01.skillineserver.service.QuizAttemptService;
import com.project01.skillineserver.utils.AuthenticationUtil;
import com.project01.skillineserver.utils.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final HistoryScoreUserRepository historyScoreUserRepository;

    @Override
    @Transactional
    public void save(AttemptQuizReq attemptQuizReq, Long userId) {
        QuizAttemptEntity quizAttempt = new QuizAttemptEntity();

        // handle attemptNo
        Integer attemptNo = computedAttemptNoOfQuiz(userId, attemptQuizReq.quizId());

        List<AnswerEntity> answersInDb = quizAttemptRepository.getAnswersByQuestionId(attemptQuizReq.quizId());
        Map<Long, List<AnswerEntity>> answerGroupToQuestion = answersInDb.stream()
                .collect(Collectors.groupingBy(AnswerEntity::getQuestionId));

        double totalScore = 0;
        List<AttemptAnswerEntity> attemptAnswerNeedSave = new ArrayList<>();

        for (AnswerUserReq answerUserReq : attemptQuizReq.answerUserReqs()) {
            Set<Long> correctAnswerIds = answerGroupToQuestion
                    .get(answerUserReq.questionId())
                    .stream()
                    .map(AnswerEntity::getId)
                    .collect(Collectors.toSet());

            boolean isCorrect = correctAnswerIds.contains(answerUserReq.answerId());
            double score = isCorrect ? answerUserReq.score() : 0D;
            totalScore += score;

            attemptAnswerNeedSave.add(AttemptAnswerEntity.builder()
                    .questionId(answerUserReq.questionId())
                    .score(score)
                    .answerId(answerUserReq.answerId())
                    .answerText(answerUserReq.answerText())
                    .build());
        }

        quizAttempt.setAttemptNo(attemptNo);
        quizAttempt.setQuizId(attemptQuizReq.quizId());
        quizAttempt.setSubmittedAt(Instant.now());
        quizAttempt.setUserId(userId);
        quizAttempt.setTotalScore(totalScore);
        quizAttemptRepository.save(quizAttempt);

        //save lich su thi
        final Long attemptId = quizAttempt.getId();
        attemptAnswerNeedSave.forEach(h -> h.setAttemptQuizId(attemptId));
        historyScoreUserRepository.saveAll(attemptAnswerNeedSave);
    }

    @Override
    public PageResponse<QuizAttemptProjection> getQuizAttempts(int page, int size, String sort, String keyword) {
        Sort sortField = MapUtil.parseSort(sort);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        CustomUserDetail customUserDetail = AuthenticationUtil.getUserDetail();

        Page<QuizAttemptProjection> pageQuizAttempt = quizAttemptRepository
                .getPageQuizAttemptOfUser(customUserDetail.getUser().getId(), keyword, pageRequest);

        return PageResponse.<QuizAttemptProjection>builder()
                .totalPages(pageQuizAttempt.getTotalPages())
                .size(size)
                .page(page)
                .list(pageQuizAttempt.getContent())
                .totalElements(pageQuizAttempt.getTotalElements())
                .build();
    }

    private Integer computedAttemptNoOfQuiz(Long userId, Long quizId) {
        Integer attemptNo = quizAttemptRepository.getAttemptNoOfUser(userId, quizId);
        attemptNo = attemptNo == null ? 0 : attemptNo;
        if (attemptNo >= 5) {
            throw new AppException(ErrorCode.QUIZ_MAX_FIVE);
        }
        attemptNo = attemptNo + 1; // thi toi da 5 lan

        return attemptNo;
    }
}
