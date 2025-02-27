package com.ginthub.ggruzdov.outbox.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginthub.ggruzdov.outbox.orderservice.component.TraceContextUtil;
import com.ginthub.ggruzdov.outbox.orderservice.model.OrderOutbox;
import com.ginthub.ggruzdov.outbox.orderservice.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final TraceContextUtil traceContextUtil;
    private final ObjectMapper objectMapper;
    private final OrderOutboxRepository orderOutboxRepository;

    @SneakyThrows
    @Transactional
    public OrderOutbox save(String aggregateId, String eventType, Object payload) {
        log.debug("Saving outbox event, aggId = {}, eventType = {}", aggregateId, eventType);
        return orderOutboxRepository.save(
            OrderOutbox.builder()
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(objectMapper.writeValueAsString(payload))
                .traceparent(traceContextUtil.getTraceparent())
                .build()
        );
    }
}
