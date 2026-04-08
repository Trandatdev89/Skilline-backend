package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.OrderReq;
import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.projection.OrderProjection;
import com.project01.skillineserver.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("@authorizationService.isAdmin()")
    public ApiResponse<PageResponse<OrderProjection>> getOrders(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) String sort,
                                               @RequestParam(required = false) String keyword) {

        return ApiResponse.<PageResponse<OrderProjection>>builder()
                .code(200)
                .message("Success")
                .data(orderService.getOrders(page,size,sort,keyword))
                .build();
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<OrderEntity> getOrderById(@PathVariable String orderId) {

        return ApiResponse.<OrderEntity>builder()
                .code(200)
                .message("Success")
                .data(orderService.getOrderById(orderId))
                .build();
    }

    @GetMapping(value = "/order-detail/{orderId}")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<List<CourseResponse>> getOrderDetailByOrderId(@PathVariable Long orderId) {
        return ApiResponse.<List<CourseResponse>>builder()
                .code(200)
                .message("Success")
                .data(orderService.getOrderDetailByOrderId(orderId))
                .build();
    }

    @PostMapping
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<OrderEntity> saveOrder(@RequestBody OrderReq orderReq, @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        return ApiResponse.<OrderEntity>builder()
                .code(200)
                .message("Success")
                .data(orderService.saveOrder(orderReq))
                .build();
    }
}
