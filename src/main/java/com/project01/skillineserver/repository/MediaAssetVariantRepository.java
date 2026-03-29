package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.MediaAssetVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// ========== MediaAssetVariantRepository.java ==========
public interface MediaAssetVariantRepository extends JpaRepository<MediaAssetVariantEntity, String> {
    List<MediaAssetVariantEntity> findByAssetId(String assetId);

    void deleteByAssetId(String assetId);
}

