package com.project01.skillineserver.dto.request;

import com.project01.skillineserver.enums.PaymentMethod;
import com.project01.skillineserver.enums.PaymentStatus;

public record PaymentReq(Long id, PaymentMethod paymentMethod
        , String orderId, PaymentStatus paymentStatus) {
}
