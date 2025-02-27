package com.ginthub.ggruzdov.outbox.orderservice.controller;

import com.ginthub.ggruzdov.outbox.orderservice.request.CreateOrderRequest;
import com.ginthub.ggruzdov.outbox.orderservice.response.CreateOrderResponse;
import com.ginthub.ggruzdov.outbox.orderservice.service.OrderFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping
    public CreateOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        var order = orderFacade.createOrderAndNotify(request);
        return new CreateOrderResponse(order.getId(), order.getStatus());
    }
}
