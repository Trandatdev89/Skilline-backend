package com.project01.skillineserver.repository;

import com.project01.skillineserver.dto.projection.OrderProjection;
import com.project01.skillineserver.entity.CourseEntity;
import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;


public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    @Query("SELECT od.id AS id, " +
            "od.status AS status, " +
            "od.createdAt AS createdAt, " +
            "od.updatedAt AS updatedAt, " +
            "od.createdBy AS createdBy, " +
            "od.updatedBy AS updatedBy, " +
            "od.totalPrice AS totalPrice, " +
            "us.username AS username, " +
            "us.address AS address, " +
            "us.fullname AS fullname, " +
            "us.email AS email, " +
            "us.phone AS phone " +
            "FROM OrderEntity od " +
            "LEFT JOIN UserEntity us ON us.id = od.userId " +
            "WHERE :keyword IS NULL OR LOWER(us.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<OrderProjection> getOrders(String keyword, Pageable pageable);

    @Query("SELECT od.id AS id, " +
            "od.status AS status, " +
            "od.createdAt AS createdAt, " +
            "od.updatedAt AS updatedAt, " +
            "od.createdBy AS createdBy, " +
            "od.updatedBy AS updatedBy, " +
            "od.totalPrice AS totalPrice, " +
            "us.username AS username, " +
            "us.address AS address, " +
            "us.fullname AS fullname, " +
            "us.email AS email, " +
            "us.phone AS phone " +
            "FROM OrderEntity od " +
            "LEFT JOIN UserEntity us ON us.id = od.userId " +
            "WHERE us.id = :userId and " +
            ":keyword IS NULL OR LOWER(us.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<OrderProjection> getOrdersMySelf(String keyword, Pageable pageable, Long userId);

    @Query("""
            select co from OrderEntity ord
            inner join OrderDetailEntity od on ord.id = od.orderId
            inner join CourseEntity co on co.id = od.courseId
            where od.orderId = :orderId
            """)
    List<CourseEntity> getOrderDetailByOrderId(String orderId);

    List<OrderEntity> findAllByStatusAndExpiresAtBefore(OrderStatus status, Instant now);
}
