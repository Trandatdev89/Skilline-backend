package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.MediaJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// ========== MediaJobRepository.java ==========
public interface MediaJobRepository extends JpaRepository<MediaJobEntity, String> {
    List<MediaJobEntity> findByAssetId(String assetId);

    @Modifying
    @Query("update MediaJobEntity j set j.status = :status, j.errorMessage = :error where j.id = :id")
    void updateStatus(@Param("id") String id,
                      @Param("status") com.project01.skillineserver.enums.JobStatus status,
                      @Param("error") String errorMessage);
}