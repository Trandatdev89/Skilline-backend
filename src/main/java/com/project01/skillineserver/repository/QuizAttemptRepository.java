package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.AnswerEntity;
import com.project01.skillineserver.entity.QuizAttemptEntity;
import com.project01.skillineserver.projection.HistoryExamFlatProjection;
import com.project01.skillineserver.projection.QuizAttemptProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttemptEntity, Long> {

    void deleteAllByQuizIdIn(List<Long> quizIds);

    QuizAttemptEntity findByQuizId(Long quizId);

    @Query("select qa.attemptNo as attemptNo " +
            "from QuizAttemptEntity qa " +
            "where qa.userId = ?1 and qa.quizId = ?2 " +
            "order by qa.attemptNo desc limit 1")
    Integer getAttemptNoOfUser(Long userId, Long quizId);

    @Query("select an from AnswerEntity an " +
            "inner join QuestionEntity qe on qe.id=an.questionId " +
            "inner join QuizEntity qu on qu.id=qe.quizId " +
            "where qu.id = ?1 and an.isCorrect = true")
    List<AnswerEntity> getAnswersByQuestionId(Long quizId);

    @Query("select qa.id from QuizAttemptEntity qa " +
            "where ?1 > qa.submittedAt " +
            "order by qa.submittedAt asc limit ?2")
    List<Long> findOldAttemptIds(Instant cutoffDate,Long batchSize);

    int deleteByIdIn(List<Long> quizAttemptIds);

    @Query("select qa.quizId as quizId," +
            "qa.userId as userId," +
            "qa.attemptNo as attemptNo," +
            "qa.submittedAt as submittedAt," +
            "qu.title as title,"+
            "us.fullname as fullName,"+
            "qa.totalScore as totalScore " +
            "from QuizAttemptEntity qa " +
            "inner join QuizEntity qu on qu.id = qa.quizId " +
            "inner join UserEntity us on us.id = qa.userId " +
            "where qa.userId=?1 and (?2 is null or qu.title like lower(concat('%',?2,'%')))" +
            "order by qa.totalScore desc ")
    Page<QuizAttemptProjection> getPageQuizAttemptOfUser(Long userId, String keyword, PageRequest pageRequest);

    @Query("select qa from QuizAttemptEntity qa " +
            "where qa.id = ?1")
    QuizAttemptEntity findQuizAttemptOfUserById(Long id);

    @Query(value = """
            SELECT
                qa.id                    AS attemptId,
                qa.submitted_at          AS submittedAt,
                qa.total_score           AS totalScore,

                qs.id                    AS questionId,
                qs.content               AS questionContent,
                qs.type                  AS questionType,
                qs.score                 AS maxScore,

                a.id                     AS answerId,
                a.content                AS answerContent,
                a.is_correct             AS isCorrect,

                IF(hauc.answer_id IS NOT NULL, 1, 0) AS isUserSelected,
                hsu.score                AS scoreAchieved
            FROM quiz_attempt qa
            JOIN questions qs ON qs.quiz_id = qa.quiz_id
            JOIN answer a ON a.question_id = qs.id
            LEFT JOIN history_score_user hsu
                   ON hsu.attempt_quiz_id = qa.id
                  AND hsu.question_id = qs.id
            LEFT JOIN history_answer_user_choice hauc
                   ON hauc.history_answer_user_id = hsu.id
                  AND hauc.answer_id = a.id
            WHERE qa.id = ?1
            """,nativeQuery = true)
    List<HistoryExamFlatProjection> getA(Long attemptQuizId);
}
