package com.project01.skillineserver.dto.reponse;

import com.project01.skillineserver.enums.ExpireUnit;
import com.project01.skillineserver.enums.LevelEnum;
import com.project01.skillineserver.enums.PublishStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse extends AuditCommonRes {
    private Long id;
    private String title;
    private LevelEnum level;
    private String thumbnail_url;
    private String description;
    private boolean isDelete;
    private BigDecimal price;
    private BigDecimal discount;
    private Double rate;
    private Long categoryId;
    private PublishStatus publishStatus;
    private ExpireUnit expireUnit;
    private Integer accessDurationValue;
}
