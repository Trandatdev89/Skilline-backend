package com.project01.skillineserver.dto.reponse;

import com.project01.skillineserver.enums.LevelEnum;
import com.project01.skillineserver.enums.PublishStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse {
    private Long id;
    private String title;
    private LevelEnum level;
    private String thumbnail_url;
    private String description;
    private boolean status;
    private BigDecimal price;
    private BigDecimal discount;
    private Double rate;
    private String createAt;
    private String updateAt;
    private Long categoryId;
    private PublishStatus publishStatus;
}
