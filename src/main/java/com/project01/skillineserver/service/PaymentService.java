package com.project01.skillineserver.service;

import com.project01.skillineserver.kafka.event.TransactionPaymentEvent;

public interface PaymentService {

    void savePayment(TransactionPaymentEvent event);
}
