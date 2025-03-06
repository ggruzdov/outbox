package com.ginthub.ggruzdov.outbox.deliveryservice.response;

import com.ginthub.ggruzdov.outbox.deliveryservice.model.Delivery;

import java.time.Instant;

public record DeliveryDetailsResponse(
    Integer id,
    String status,
    Instant deliveryDate
) {
    public static DeliveryDetailsResponse from(Delivery delivery) {
        return new DeliveryDetailsResponse(delivery.getId(), delivery.getStatus(), delivery.getDeliveryDate());
    }
}
