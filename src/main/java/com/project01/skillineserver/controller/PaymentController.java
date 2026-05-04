package com.project01.skillineserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.reponse.VNPayResponse;
import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.OrderStatus;
import com.project01.skillineserver.enums.PaymentMethod;
import com.project01.skillineserver.enums.PaymentStatus;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.kafka.event.TransactionPaymentEvent;
import com.project01.skillineserver.properties.KafkaTopicProperties;
import com.project01.skillineserver.repository.OrderRepository;
import com.project01.skillineserver.service.EnrollmentService;
import com.project01.skillineserver.service.PaymentService;
import com.project01.skillineserver.vnpay.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicProperties kafkaTopicProperties;
    private final PaymentService paymentService;
    private final EnrollmentService enrollmentService;

    @Value("${domain.client}")
    @NonFinal
    private String DOMAIN_CLIENT;

    @GetMapping(value = "/api/payment")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public VNPayResponse submitOrder(@RequestParam("orderId") String id,
                                     @RequestParam("amount") int orderTotal,
                                     @RequestParam("courses") List<Long> courses,
                                     @AuthenticationPrincipal CustomUserDetail customUserDetail,
                                     HttpServletRequest request) throws JsonProcessingException {


        Map<String, Object> extra = new HashMap<>();
        extra.put("userId", customUserDetail.getUser().getId());
        extra.put("courses", courses);
        ObjectMapper mapper = new ObjectMapper();
        String orderInfoJson = mapper.writeValueAsString(extra);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(orderTotal * 100, orderInfoJson, baseUrl, id);

        return VNPayResponse.builder()
                .URL(vnpayUrl)
                .status(200L)
                .message("Success")
                .build();
    }

    @GetMapping("/vnpay-payment/{orderId}")
    @Transactional
    public ResponseEntity<Void> createPayment(@PathVariable String orderId
            , @RequestParam Map<String, String> params
            , HttpServletRequest request) throws JsonProcessingException {

        int paymentStatus = vnPayService.orderReturn(request);
        if (paymentStatus != 1) {
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        }

        OrderEntity order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        String orderInfoJson = params.get("vnp_OrderInfo");

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> extra = mapper.readValue(orderInfoJson, new TypeReference<Map<String, Object>>() {
        });

        Long userId = Long.valueOf(extra.get("userId").toString());

        List<Long> courses = ((List<?>) extra.get("courses"))
                .stream()
                .map(val -> Long.valueOf(val.toString()))
                .toList();

        BigDecimal amount = BigDecimal.valueOf(Double.valueOf(params.get("vnp_Amount")) / 100);

        handleProcessTransaction(amount, orderId);

        handleProcessEnrollment(courses, userId);

        String targetUrl = DOMAIN_CLIENT + "/success";
        org.springframework.http.HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(targetUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private void handleProcessTransaction(BigDecimal amount, String orderId) {
        TransactionPaymentEvent paymentEvent = TransactionPaymentEvent.builder()
                .paymentStatus(PaymentStatus.SUCCESS)
                .amount(amount)
                .orderId(orderId)
                .paymentId(123l)
                .transactionId("test13")
                .gatewayResponse("Giao dich tam thoi")
                .paymentMethod(PaymentMethod.VNPAY)
                .build();

        paymentService.savePayment(paymentEvent);
    }

    private void handleProcessEnrollment(List<Long> courseIds, Long userId) {
        enrollmentService.saveEnrollment(courseIds, userId);
    }

}
