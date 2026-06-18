package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.OrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity,Long> {
    void deleteAllByOrderIdIn(List<String> orderIds);
}
