package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.MediaAssetEntity;
import com.project01.skillineserver.enums.ProcessStatus;
import com.project01.skillineserver.enums.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MediaAssetRepository extends JpaRepository<MediaAssetEntity, String> {

    Optional<MediaAssetEntity> findByObjectKey(String objectKey);

    @Modifying
    @Query("update MediaAssetEntity m set m.uploadStatus = :status where m.id = :id")
    void updateUploadStatus(@Param("id") String id, @Param("status") UploadStatus status);

    @Modifying
    @Query("update MediaAssetEntity m set m.processStatus = :status, m.hlsMasterKey = :hlsKey where m.id = :id")
    void updateProcessStatus(@Param("id") String id,
                             @Param("status") ProcessStatus status,
                             @Param("hlsKey") String hlsMasterKey);
}

