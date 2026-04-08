package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.QuizReq;
import com.project01.skillineserver.entity.QuestionEntity;
import com.project01.skillineserver.entity.QuizAttemptEntity;
import com.project01.skillineserver.entity.QuizEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.QuestionRepository;
import com.project01.skillineserver.repository.QuizAttemptRepository;
import com.project01.skillineserver.repository.QuizRepository;
import com.project01.skillineserver.service.QuizService;
import com.project01.skillineserver.utils.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionRepository questionRepository;

    @Override
    @Transactional
    public void save(QuizReq quizReq) {

        QuizEntity quizEntity = Optional
                .ofNullable(quizReq.id())
                .flatMap(quizRepository::findById)
                .orElseGet(QuizEntity::new);

        quizEntity.setTitle(quizReq.title());
        quizEntity.setDescription(quizReq.desc());
        quizEntity.setLectureId(quizReq.lectureId());
        quizEntity.setTimeLimit(quizReq.timeLimit());
        quizEntity.setMaxAttempt(quizReq.maxAttempt());
        quizEntity.setTimeUnit(quizReq.timeUnit());

        quizRepository.save(quizEntity);
    }

    @Override
    public void delete(List<Long> quizIds) {

        if (quizIds == null || quizIds.isEmpty()) {
            throw new AppException(ErrorCode.QUIZ_ID_REQUIRE);
        }

        List<QuizAttemptEntity> listQuizAttemptNeedRemove = new ArrayList<>();
        List<QuestionEntity> listQuestionNeedRemove = new ArrayList<>();

        for (Long quizId : quizIds) {
            listQuizAttemptNeedRemove.add(quizAttemptRepository.findByQuizId(quizId));
            listQuestionNeedRemove.add(questionRepository.findByQuizId(quizId));
        }

        quizAttemptRepository.deleteAll(listQuizAttemptNeedRemove);
        questionRepository.deleteAll(listQuestionNeedRemove);
        quizRepository.deleteByIdIn(quizIds);
    }

    @Override
    public PageResponse<QuizEntity> getQuizByLectureId(int page, int size, String sort, String keyword, Long courseId, String lectureId) {
        Sort sortField = MapUtil.parseSort(sort);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);
        Page<QuizEntity> pageQuiz = quizRepository.getQuizzes(keyword, courseId, lectureId, pageRequest);
        return PageResponse.<QuizEntity>builder()
                .totalElements(pageQuiz.getTotalElements())
                .totalPages(pageQuiz.getTotalPages())
                .page(page)
                .size(size)
                .list(pageQuiz.getContent())
                .build();
    }
}
