package com.ginthub.ggruzdov.outbox.deliveryservice.messaging.event;

public record OrderCreatedEvent(
    Integer id,
    Integer customerId,
    Integer totalPrice
) {
}
