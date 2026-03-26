package com.project01.skillineserver.entity;

import com.project01.skillineserver.enums.LevelEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "courses")
public class CourseEntity extends BaseEntity<Long> {
    private String title;
    private String description;
    @Column(name = "category_id")
    private Long categoryId;
    @Enumerated(EnumType.STRING)
    private LevelEnum level;
    @Column(name = "thumbnail_asset_id")
    private String thumbnailAssetId;
    private boolean status;
    private BigDecimal price;
    private Double rate;
    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;
}
