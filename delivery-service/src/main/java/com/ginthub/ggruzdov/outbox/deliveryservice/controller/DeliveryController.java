package com.ginthub.ggruzdov.outbox.deliveryservice.controller;

import com.ginthub.ggruzdov.outbox.deliveryservice.response.DeliveryDetailsResponse;
import com.ginthub.ggruzdov.outbox.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public DeliveryDetailsResponse getDeliveryDetails(@RequestParam Integer orderId) {
        var delivery = deliveryService.findByOrderId(orderId);
        return DeliveryDetailsResponse.from(delivery);
    }
}
