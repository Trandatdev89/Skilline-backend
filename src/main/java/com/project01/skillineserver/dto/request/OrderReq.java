package com.project01.skillineserver.dto.request;

import com.project01.skillineserver.enums.OrderStatus;
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
    private Long userId;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private List<Long> courseId;
}
