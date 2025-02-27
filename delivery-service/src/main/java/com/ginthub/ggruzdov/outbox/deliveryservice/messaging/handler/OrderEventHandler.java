package com.ginthub.ggruzdov.outbox.deliveryservice.messaging.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginthub.ggruzdov.outbox.deliveryservice.messaging.MessageSkipPolicyManager;
import com.ginthub.ggruzdov.outbox.deliveryservice.messaging.event.OrderCreatedEvent;
import com.ginthub.ggruzdov.outbox.deliveryservice.messaging.event.Outbox;
import com.ginthub.ggruzdov.outbox.deliveryservice.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;
    private final MessageSkipPolicyManager messageSkipPolicyManager;

    @SneakyThrows
    @KafkaListener(topics = "${messaging.queues.orders}")
    public void handle(@Valid @Payload Outbox outbox) {
        log.info("Received order event: {}, aggId: {}", outbox.eventType(), outbox.aggregateId());
        try {
            if ("ORDER_CREATED".equals(outbox.eventType())) {
                var orderCreatedEvent = objectMapper.readValue(outbox.payload(), OrderCreatedEvent.class);
                deliveryService.create(orderCreatedEvent);
            }
        } catch (Exception e) {
            // We want to see what error occurred during retries
            log.error("Error processing order event", e);
            // Here is an auxiliary component used to skip a message forcibly
            // in order not to get into infinite retry loop
            if (messageSkipPolicyManager.shouldSkip(outbox)) {
                return;
            }
            throw e;
        }
    }
}
