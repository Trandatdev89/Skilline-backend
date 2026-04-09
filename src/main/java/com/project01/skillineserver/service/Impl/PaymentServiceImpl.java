package com.project01.skillineserver.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project01.skillineserver.entity.PaymentEntity;
import com.project01.skillineserver.kafka.event.TransactionPaymentEvent;
import com.project01.skillineserver.repository.PaymentRepository;
import com.project01.skillineserver.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.payment-transaction:payment-transaction}",
            groupId = "media-processing-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void createTransactionPayment(ConsumerRecord<String, Object> record, Acknowledgment ack) {

        log.info("[Kafka] Received media.uploaded | assetId={}", record.key());

        TransactionPaymentEvent event;
        try {
            event = objectMapper.convertValue(record.value(), TransactionPaymentEvent.class);
        } catch (Exception e) {
            log.error("Cannot deserialize event key={}: {}", record.key(), e.getMessage());
            ack.acknowledge();
            return;
        }

        PaymentEntity paymentEntity = Optional.ofNullable(event.getPaymentId()).
                flatMap(paymentRepository::findById)
                .orElseGet(PaymentEntity::new);

        paymentEntity.setPaymentMethod(event.getPaymentMethod());
        paymentEntity.setTransactionId(event.getTransactionId());
        paymentEntity.setAmount(event.getAmount());
        paymentEntity.setStatus(event.getPaymentStatus());
        paymentEntity.setOrderId(event.getOrderId());
        paymentEntity.setGatewayResponse(event.getGatewayResponse());
        paymentEntity.setPaidAt(Instant.now());

        paymentRepository.save(paymentEntity);
    }
}
