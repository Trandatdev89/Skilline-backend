package com.project01.skillineserver.dto.request;

import com.project01.skillineserver.enums.PaymentMethod;
import com.project01.skillineserver.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentReq(PaymentMethod paymentMethod
        , String orderId, PaymentStatus paymentStatus,BigDecimal amount,
                         String transactionId,String gatewayResponse) {
}
