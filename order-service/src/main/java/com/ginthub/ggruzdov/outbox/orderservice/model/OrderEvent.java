package com.ginthub.ggruzdov.outbox.orderservice.model;

public record OrderEvent(Order order, OrderOutbox outbox) {
}
