package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.projection.OrderProjection;
import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.OrderReq;
import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.enums.Role;

import java.util.List;

public interface OrderService {
    PageResponse<OrderProjection> getOrders(int page, int size, String sort, String keyword);

    OrderEntity getOrderById(String id, Long userId, Role role);

    OrderEntity saveOrder(OrderReq orderReq, Long userId);

    List<CourseResponse> getOrderDetailByOrderId(String orderId);

    PageResponse<OrderProjection> getOrdersMySelf(int page, int size, String sort, String keyword, Long userId);
}
