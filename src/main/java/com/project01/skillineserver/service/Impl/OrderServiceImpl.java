package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.CourseResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.OrderReq;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.entity.OrderDetailEntity;
import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.entity.UserEntity;
import com.project01.skillineserver.enums.*;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.kafka.event.TransactionPaymentEvent;
import com.project01.skillineserver.mapper.CourseMapper;
import com.project01.skillineserver.projection.OrderProjection;
import com.project01.skillineserver.properties.KafkaTopicProperties;
import com.project01.skillineserver.repository.CourseRepository;
import com.project01.skillineserver.repository.OrderDetailRepository;
import com.project01.skillineserver.repository.OrderRepository;
import com.project01.skillineserver.repository.UserRepository;
import com.project01.skillineserver.service.EnrollmentService;
import com.project01.skillineserver.service.OrderService;
import com.project01.skillineserver.utils.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final EnrollmentService enrollmentService;
    private final MapUtil mapUtil;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicProperties kafkaTopicProperties;
    private final CourseMapper courseMapper;

    @Override
    public PageResponse<OrderProjection> getOrders(int page, int size, String sort, String keyword) {
        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<OrderProjection> orders = orderRepository.getOrders(keyword, pageRequest);

        return PageResponse.<OrderProjection>builder()
                .list(orders.getContent())
                .page(page)
                .size(size)
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
    }

    @Override
    public OrderEntity getOrderById(String id, Long userId, Role role) {

        OrderEntity orderInDB = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (role != Role.ADMIN && !Objects.equals(orderInDB.getUserId(), userId)) {
            throw new AppException(ErrorCode.FOBIDEN);
        }
        return orderInDB;
    }

    @Override
    @Transactional
    public OrderEntity saveOrder(OrderReq orderReq, Long userId) {

        if (userId == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        if (orderReq.getCourseId().isEmpty()) {
            throw new AppException(ErrorCode.COURSE_EMPTY);
        }

        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<CourseEntity> courseEntityList = courseRepository
                .findAllByCourseStatusPublishIdIn(orderReq.getCourseId(), PublishStatus.PUBLISHER)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_PUBLISHER));

        if (enrollmentService.checkUserEnrollment(orderReq.getCourseId(), userId)) {
            throw new AppException(ErrorCode.COURSE_BOUGHT_ALREADY);
        }

        OrderEntity orderEntity = OrderEntity.builder()
                .userId(user.getId())
                .status(OrderStatus.PENDING)
                .totalPrice(orderReq.getTotalPrice())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        OrderEntity order = orderRepository.save(orderEntity);

        List<OrderDetailEntity> orderDetailEntities = new ArrayList<>();
        for (CourseEntity item : courseEntityList) {
            orderDetailEntities.add(OrderDetailEntity.builder()
                    .orderId(order.getId())
                    .courseId(item.getId())
                    .discountPrice(item.getPriceDiscount())
                    .discount(item.getDiscount())
                    .originalPrice(item.getPriceOriginal())
                    .build());
        }

        orderDetailRepository.saveAll(orderDetailEntities);

        TransactionPaymentEvent event = TransactionPaymentEvent.builder()
                .paymentMethod(PaymentMethod.VNPAY)
                .amount(orderReq.getTotalPrice())
                .paymentStatus(PaymentStatus.PENDING)
                .orderId(order.getId())
                .build();

        kafkaTemplate.send(kafkaTopicProperties.getPaymentTransaction(), order.getId(), event);
        log.info("Published transaction payment event for order [{}]",
                order.getId());

        return order;
    }

    @Override
    public List<CourseResponse> getOrderDetailByOrderId(String orderId) {
        List<CourseEntity> courseInDB = orderRepository.getOrderDetailByOrderId(orderId);
        return mapUtil.handleComputedThumbnail(courseInDB, CourseEntity::getThumbnailAssetId, courseMapper::toCourseResponse);
    }

    @Override
    public PageResponse<OrderProjection> getOrdersMySelf(int page, int size, String sort, String keyword, Long userId) {
        Sort sortField = MapUtil.parseSort(sort);

        PageRequest pageRequest = PageRequest.of(page - 1, size, sortField);

        Page<OrderProjection> orders = orderRepository.getOrdersMySelf(keyword, pageRequest, userId);

        return PageResponse.<OrderProjection>builder()
                .list(orders.getContent())
                .page(page)
                .size(size)
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
    }
}
