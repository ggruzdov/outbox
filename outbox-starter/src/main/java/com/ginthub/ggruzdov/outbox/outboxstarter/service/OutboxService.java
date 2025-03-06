package com.ginthub.ggruzdov.outbox.outboxstarter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginthub.ggruzdov.outbox.outboxstarter.component.TraceContextUtil;
import com.ginthub.ggruzdov.outbox.outboxstarter.model.Outbox;
import com.ginthub.ggruzdov.outbox.outboxstarter.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final TraceContextUtil traceContextUtil;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    @SneakyThrows
    @Transactional(propagation = Propagation.MANDATORY)
    public Outbox save(String aggregateId, String eventType, Object payload) {
        log.debug("Saving outbox event, aggId = {}, eventType = {}", aggregateId, eventType);
        var outbox = Outbox.builder()
            .id(UUID.randomUUID())
            .aggregateId(aggregateId)
            .eventType(eventType)
            .payload(objectMapper.writeValueAsString(payload))
            .traceparent(traceContextUtil.getTraceparent())
            .createdAt(Instant.now())
            .build();

        outboxRepository.save(outbox);

        return outbox;
    }
}
