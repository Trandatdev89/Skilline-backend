package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findReviewEntitiesByCourseId(Long courseId);
}
