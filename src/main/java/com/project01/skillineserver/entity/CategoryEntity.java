package com.project01.skillineserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "category")
public class CategoryEntity extends UuidEntity<String> {

    private String name;

    @Column(name = "slug", unique = true)
    private String slug;

    private boolean isActive;

    @Column(name = "thumbnail_asset_id")
    private String thumbnailAssetId;
}
