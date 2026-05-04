package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.QuizEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {
    void deleteByIdIn(List<Long> ids);

    Optional<QuizEntity> findByLectureId(String lectureId);


    @Query(value = "select qe from QuizEntity qe " +
            "left join LectureEntity le on le.id=qe.lectureId " +
            "where (?2 is null or le.courseId = ?2) and " +
            "(?1 is null or lower(qe.title) like lower(concat('%',?1,'%')) ) ",
            countQuery = """
                      select count(qe)
                      from QuizEntity qe
                      left join LectureEntity le on le.id = qe.lectureId
                      where (?2 is null or le.courseId = ?2)
                        and (?3 is null or qe.lectureId = ?3)
                        and (?1 is null 
                             or lower(qe.title) like lower(concat('%',?1,'%')))
                    """)
    Page<QuizEntity> getQuizzes(String keyword, Long courseId, Pageable pageable);
}
