package com.project01.skillineserver.dto.request;

import com.project01.skillineserver.enums.LevelEnum;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record CourseReq(Long id, String title, String desc,Double rate,
                        LevelEnum level, BigDecimal price, BigDecimal discount,
                        MultipartFile thumbnail, Long categoryId, Boolean isDeleted) {
}
