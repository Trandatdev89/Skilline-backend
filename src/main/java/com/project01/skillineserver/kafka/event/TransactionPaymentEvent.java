package com.project01.skillineserver.kafka.event;

import com.project01.skillineserver.enums.PaymentMethod;
import com.project01.skillineserver.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPaymentEvent {
    private Long paymentId;
    private String orderId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private String gatewayResponse;
}
