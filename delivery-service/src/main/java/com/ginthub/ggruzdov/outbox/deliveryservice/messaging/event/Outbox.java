package com.ginthub.ggruzdov.outbox.deliveryservice.messaging.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record Outbox(
    @NotNull
    UUID id,
    @NotBlank
    String aggregateId,
    @NotBlank
    String eventType,
    @NotBlank
    String payload
) {
}
