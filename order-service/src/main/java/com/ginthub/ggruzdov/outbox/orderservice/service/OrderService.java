package com.ginthub.ggruzdov.outbox.orderservice.service;

import com.ginthub.ggruzdov.outbox.orderservice.model.Order;
import com.ginthub.ggruzdov.outbox.orderservice.model.OrderEvent;
import com.ginthub.ggruzdov.outbox.orderservice.repository.OrderRepository;
import com.ginthub.ggruzdov.outbox.orderservice.request.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    @Transactional
    public OrderEvent create(CreateOrderRequest request) {
        var order = orderRepository.save(
            Order.builder()
                .customerId(request.customerId())
                .totalPrice(request.totalPrice())
                .status("NEW")
                .build()
        );

        var outbox = outboxService.save(order.getId().toString(), "ORDER_CREATED", order);

        return new OrderEvent(order, outbox);
    }
}
