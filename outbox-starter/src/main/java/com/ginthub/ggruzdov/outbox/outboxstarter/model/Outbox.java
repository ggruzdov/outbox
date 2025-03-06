package com.ginthub.ggruzdov.outbox.outboxstarter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Outbox {
    private UUID id;
    private String aggregateId;
    private String eventType;
    private String traceparent;
    private String payload;
    private Instant createdAt;
    private Instant processedAt;
}
