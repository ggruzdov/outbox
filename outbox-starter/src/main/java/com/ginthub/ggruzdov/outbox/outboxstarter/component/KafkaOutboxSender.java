package com.ginthub.ggruzdov.outbox.outboxstarter.component;

import com.ginthub.ggruzdov.outbox.outboxstarter.model.Outbox;
import com.ginthub.ggruzdov.outbox.outboxstarter.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnMissingBean(type = "OutboxSender")
@RequiredArgsConstructor
public class KafkaOutboxSender implements OutboxSender {

    private final KafkaTemplate<String, OutboxEvent> kafkaTemplate;
    private final OutboxCommiter outboxCommiter;

    @Override
    public void send(Outbox outbox) {
        log.debug("Sending order outbox {}", outbox.getAggregateId());
        var event = OutboxEvent.from(outbox);
        kafkaTemplate.sendDefault(event.aggregateId(), event).whenComplete((ignored, e) -> {
            if (e == null) {
                outboxCommiter.commit(outbox);
            } else {
                log.error("Error while sending message {}", outbox.getAggregateId(), e);
            }
        });
    }
}
