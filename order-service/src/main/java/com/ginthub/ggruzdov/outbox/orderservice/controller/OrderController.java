package com.ginthub.ggruzdov.outbox.orderservice.controller;

import com.ginthub.ggruzdov.outbox.orderservice.request.CreateOrderRequest;
import com.ginthub.ggruzdov.outbox.orderservice.response.CreateOrderResponse;
import com.ginthub.ggruzdov.outbox.orderservice.service.OrderFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping
    public CreateOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Got new order request: {}", request);
        var order = orderFacade.createOrderAndNotify(request);
        return new CreateOrderResponse(order.getId(), order.getStatus());
    }
}
