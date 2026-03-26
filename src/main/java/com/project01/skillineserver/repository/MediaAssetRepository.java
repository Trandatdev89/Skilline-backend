package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.MediaAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAssetEntity, String> {
}