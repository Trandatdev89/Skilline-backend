package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.LectureEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LectureRepository extends JpaRepository<LectureEntity, String> {


    Optional<LectureEntity> findByIdAndCourseId(String id, Long courseId);

    @Modifying
    @Query("update LectureEntity le set le.delete = true where le.id in :lectureIds")
    void deleteAllByLectureIdIn(List<String> lectureIds);

    @Query("select le from LectureEntity le " +
            "inner join CourseEntity co on co.id=le.courseId " +
            "where le.courseId=?2 " +
            "and le.delete = false " +
            "and co.isDelete = false " +
            "and (?1 is null or le.title like lower(concat('%',?1,'%')))")
    Page<LectureEntity> getLectures(String keyword, Long courseId, PageRequest pageRequest);


    @Query(value = """
            select max(le.position) as max_position from lecture le where le.course_id = :courseId
            """, nativeQuery = true)
    Integer findMaxPosition(Long courseId);
}
