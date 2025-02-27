package com.ginthub.ggruzdov.outbox.orderservice.service;

import com.ginthub.ggruzdov.outbox.orderservice.component.MessageSender;
import com.ginthub.ggruzdov.outbox.orderservice.model.Order;
import com.ginthub.ggruzdov.outbox.orderservice.request.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final MessageSender messageSender;

    public Order createOrderAndNotify(CreateOrderRequest request) {
        var orderEvent = orderService.create(request);
        messageSender.send(orderEvent.outbox());

        return orderEvent.order();
    }
}
