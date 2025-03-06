package com.ginthub.ggruzdov.outbox.orderservice.service;

import com.ginthub.ggruzdov.outbox.orderservice.model.Order;
import com.ginthub.ggruzdov.outbox.orderservice.request.CreateOrderRequest;
import com.ginthub.ggruzdov.outbox.outboxstarter.component.OutboxSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final OutboxSender outboxSender;

    public Order createOrderAndNotify(CreateOrderRequest request) {
        var orderEvent = orderService.create(request);
        outboxSender.send(orderEvent.outbox());

        return orderEvent.order();
    }
}
