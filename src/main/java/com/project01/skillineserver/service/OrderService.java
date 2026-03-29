package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.OrderReq;
import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.projection.OrderProjection;

import java.util.List;

public interface OrderService {
    PageResponse<OrderProjection> getOrders(int page, int size, String sort, String keyword);

    OrderEntity getOrderById(String id);
    OrderEntity saveOrder(OrderReq orderReq);
    List<CourseResponse> getOrderDetailByOrderId(Long orderId);
}
