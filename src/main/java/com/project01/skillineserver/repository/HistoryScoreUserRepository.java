package com.project01.skillineserver.repository;

import com.project01.skillineserver.dto.projection.AnswerUserChoiceProjection;
import com.project01.skillineserver.entity.AttemptAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface HistoryScoreUserRepository extends JpaRepository<AttemptAnswerEntity, Long> {

    @Query("""
                select hsu.score as score,
                hsu.answerId as answerId,
                hsu.questionId as questionId
                from AttemptAnswerEntity hsu
                where hsu.attemptQuizId = ?1                          
            """)
    List<AnswerUserChoiceProjection> findByAttemptQuizIdAndQuestionIdIn(Long attemptQuizId);

}
