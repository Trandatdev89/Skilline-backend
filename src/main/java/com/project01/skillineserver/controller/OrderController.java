package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.OrderReq;
import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.enums.Role;
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
                .data(orderService.getOrders(page, size, sort, keyword))
                .build();
    }

    @GetMapping(value = "/my-self")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<PageResponse<OrderProjection>> getOrdersMySelf(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size,
                                                                      @RequestParam(required = false) String sort,
                                                                      @RequestParam(required = false) String keyword,
                                                                      @AuthenticationPrincipal CustomUserDetail customUserDetail) {

        Long userId = customUserDetail.getUser().getId();

        return ApiResponse.<PageResponse<OrderProjection>>builder()
                .code(200)
                .message("Success")
                .data(orderService.getOrdersMySelf(page, size, sort, keyword, userId))
                .build();
    }


    @GetMapping(value = "/{id}")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<OrderEntity> getOrderById(@PathVariable String id, @AuthenticationPrincipal CustomUserDetail customUserDetail) {

        Long userId = customUserDetail.getUser().getId();
        Role role = customUserDetail.getUser().getRole();

        return ApiResponse.<OrderEntity>builder()
                .code(200)
                .message("Success")
                .data(orderService.getOrderById(id, userId, role))
                .build();
    }

    @GetMapping(value = "/order-detail/{orderId}")
    @PreAuthorize("@authorizationService.isAdmin()")
    public ApiResponse<List<CourseResponse>> getOrderDetailByOrderId(@PathVariable String orderId) {
        return ApiResponse.<List<CourseResponse>>builder()
                .code(200)
                .message("Success")
                .data(orderService.getOrderDetailByOrderId(orderId))
                .build();
    }

    @PostMapping
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<OrderEntity> saveOrder(@RequestBody OrderReq orderReq, @AuthenticationPrincipal CustomUserDetail customUserDetail) {

        Long userId = customUserDetail.getUser().getId();

        return ApiResponse.<OrderEntity>builder()
                .code(200)
                .message("Success")
                .data(orderService.saveOrder(orderReq, userId))
                .build();
    }
}
