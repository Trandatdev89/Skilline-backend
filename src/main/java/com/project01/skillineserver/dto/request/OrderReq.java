package com.project01.skillineserver.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderReq {
    private Long id;
    private BigDecimal totalPrice;
    private List<Long> courseId;
}
