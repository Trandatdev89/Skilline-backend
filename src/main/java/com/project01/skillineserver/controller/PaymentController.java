package com.project01.skillineserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project01.skillineserver.dto.reponse.VNPayResponse;
import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.entity.PaymentEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.enums.OrderStatus;
import com.project01.skillineserver.enums.PaymentMethod;
import com.project01.skillineserver.enums.PaymentStatus;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.OrderRepository;
import com.project01.skillineserver.repository.PaymentRepository;
import com.project01.skillineserver.service.CourseService;
import com.project01.skillineserver.vnpay.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;
    private final CourseService courseService;

    @Value("${domain.client}")
    @NonFinal
    private String DOMAIN_CLIENT;

    @GetMapping(value = "/api/payment")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public VNPayResponse submitOrder(@RequestParam("orderId") int id,
                                     @RequestParam("amount") int orderTotal,
                                     @RequestParam("courses") List<Long> courses,
                                     @RequestParam("userId") Long userId,
                                     HttpServletRequest request) throws JsonProcessingException {

        Map<String, Object> extra = new HashMap<>();
        extra.put("userId", userId);
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

    @GetMapping("/vnpay-payment/{id}")
    @Transactional
    public ResponseEntity<Void> createPayment(@PathVariable String orderId, @RequestParam Map<String, String> params) throws JsonProcessingException {

        OrderEntity order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        String orderInfoJson = params.get("vnp_OrderInfo");

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> extra = mapper.readValue(orderInfoJson, new TypeReference<Map<String, Object>>() {});
        Long userId = Long.valueOf(extra.get("userId").toString());

        List<Long> courses = ((List<?>) extra.get("courses"))
                .stream()
                .map(val -> Long.valueOf(val.toString()))
                .toList();

        courseService.purchaseCourse(courses,userId);

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .paidAt(Instant.now())
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.SUCCESS)
                .amount(BigDecimal.valueOf(Double.valueOf(params.get("vnp_Amount")) / 100))
                .orderId(orderId)
                .build();

        paymentRepository.save(paymentEntity);

        String targetUrl = DOMAIN_CLIENT + "/success";
        org.springframework.http.HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(targetUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
