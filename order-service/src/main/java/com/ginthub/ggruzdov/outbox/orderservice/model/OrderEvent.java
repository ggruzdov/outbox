package com.ginthub.ggruzdov.outbox.orderservice.model;

import com.ginthub.ggruzdov.outbox.outboxstarter.model.Outbox;

public record OrderEvent(Order order, Outbox outbox) {
}
