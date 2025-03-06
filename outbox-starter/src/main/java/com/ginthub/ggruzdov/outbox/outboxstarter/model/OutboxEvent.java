package com.ginthub.ggruzdov.outbox.outboxstarter.model;

import java.util.UUID;

public record OutboxEvent(
     UUID id,
     String aggregateId,
     String eventType,
     String payload
) {
    public static OutboxEvent from(Outbox outbox) {
        return new OutboxEvent(
            outbox.getId(),
            outbox.getAggregateId(),
            outbox.getEventType(),
            outbox.getPayload()
        );
    }
}
