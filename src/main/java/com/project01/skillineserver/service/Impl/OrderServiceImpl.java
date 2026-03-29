package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.OrderReq;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.entity.OrderDetailEntity;
import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.entity.UserEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.mapper.CourseMapper;
import com.project01.skillineserver.projection.OrderProjection;
import com.project01.skillineserver.repository.CourseRepository;
import com.project01.skillineserver.repository.OrderDetailRepository;
import com.project01.skillineserver.repository.OrderRepository;
import com.project01.skillineserver.repository.UserRepository;
import com.project01.skillineserver.service.OrderService;
import com.project01.skillineserver.utils.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CourseMapper courseMapper;

    @Override
    public PageResponse<OrderProjection> getOrders(int page, int size, String sort, String keyword) {
        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<OrderProjection> orders = orderRepository.getOrders(keyword,pageRequest);

        return PageResponse.<OrderProjection>builder()
                .list(orders.getContent())
                .page(page)
                .size(size)
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
    }

    @Override
    public OrderEntity getOrderById(String id) {
        return orderRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.LECTURE_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = {AppException.class})
    public OrderEntity saveOrder(OrderReq orderReq) {

        UserEntity user = userRepository.findById(orderReq.getUserId()).orElseThrow(()->new AppException(ErrorCode.LECTURE_NOT_FOUND));

        OrderEntity orderEntity = OrderEntity.builder()
                .userId(user.getId())
                .status(orderReq.getStatus())
                .totalPrice(orderReq.getTotalPrice())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        OrderEntity order = orderRepository.save(orderEntity);

        List<CourseEntity> courseEntityList = courseRepository.findAllByCourseIdIn(orderReq.getCourseId());

        List<OrderDetailEntity> orderDetailEntities = new ArrayList<>();
        for (CourseEntity item : courseEntityList){
            orderDetailEntities.add(OrderDetailEntity.builder()
                            .orderId(order.getId())
                            .courseId(item.getId())
                            .price(item.getPrice())
                    .build());
        }
        orderDetailRepository.saveAll(orderDetailEntities);

        return order;
    }

    @Override
    public List<CourseResponse> getOrderDetailByOrderId(Long orderId) {
        List<CourseEntity> courseInDB =  orderRepository.getOrderDetailByOrderId(orderId);
        return courseInDB.stream().map(courseMapper::toLectureResponse).toList();
    }
}
