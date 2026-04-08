package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.QuestionEntity;
import com.project01.skillineserver.entity.QuizAttemptEntity;
import com.project01.skillineserver.projection.QuestionExamProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    QuestionEntity findByQuizId(Long quizId);

    List<QuestionEntity> findAllByQuizId(Long quizId);

    @Query(value = """
            select qu.id as questionId,
            qu.content as questionContent,
            qu.type as type,
            qu.score as score,
            an.id as answerId,
            an.content as answerContent
            from questions qu
            inner join answer an on qu.id=an.question_id
            where qu.quiz_id=1;
                        """, nativeQuery = true)
    List<QuestionExamProjection> findQuestionByQuizId(Long quizId);

    List<QuestionEntity> findAllByIdIn(Set<Long> ids);
}
