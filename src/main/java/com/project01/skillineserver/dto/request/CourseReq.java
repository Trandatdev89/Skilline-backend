package com.project01.skillineserver.dto.request;

import com.project01.skillineserver.enums.ExpireUnit;
import com.project01.skillineserver.enums.LevelEnum;
import com.project01.skillineserver.enums.PublishStatus;

import java.math.BigDecimal;

public record CourseReq(String id, String title, String description, Double rate,
                        LevelEnum level, BigDecimal price, BigDecimal discount,
                        String assetId, String categoryId, Integer accessDurationValue,
                        ExpireUnit accessDurationUnit, PublishStatus publishStatus) {
}
