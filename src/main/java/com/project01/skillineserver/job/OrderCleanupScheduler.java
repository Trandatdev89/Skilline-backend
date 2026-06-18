package com.project01.skillineserver.job;

import com.project01.skillineserver.entity.OrderEntity;
import com.project01.skillineserver.enums.OrderStatus;
import com.project01.skillineserver.repository.OrderDetailRepository;
import com.project01.skillineserver.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCleanupScheduler {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void cleanupExpiredOrders() {
        Instant now = Instant.now();

        List<OrderEntity> expiredOrders = orderRepository
                .findAllByStatusAndExpiresAtBefore(OrderStatus.PENDING, now);

        if (expiredOrders.isEmpty()) {
            log.info("[OrderCleanup] No expired orders found.");
            return;
        }

        List<String> orderIds = expiredOrders.stream()
                .map(OrderEntity::getId)
                .toList();

        orderDetailRepository.deleteAllByOrderIdIn(orderIds);

        orderRepository.deleteAll(expiredOrders);

        log.info("[OrderCleanup] Deleted {} expired orders.", expiredOrders.size());
    }
}