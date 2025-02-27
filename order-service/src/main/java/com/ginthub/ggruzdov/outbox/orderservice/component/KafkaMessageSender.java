package com.ginthub.ggruzdov.outbox.orderservice.component;

import com.ginthub.ggruzdov.outbox.orderservice.model.OrderOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageSender implements MessageSender {

    private final KafkaTemplate<String, OrderOutbox> kafkaTemplate;
    private final MessageCommiter messageCommiter;

    @Override
    public void send(OrderOutbox orderOutbox) {
        log.debug("Sending order outbox {}", orderOutbox.getAggregateId());
        try {
            kafkaTemplate.sendDefault(orderOutbox.getAggregateId(), orderOutbox);
            messageCommiter.commit(orderOutbox);
        } catch (Exception e) {
            log.error("Error while sending message {}", orderOutbox.getAggregateId(), e);
        }
    }
}
