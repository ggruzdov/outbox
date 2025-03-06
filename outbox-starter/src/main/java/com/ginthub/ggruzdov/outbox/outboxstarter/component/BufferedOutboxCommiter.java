package com.ginthub.ggruzdov.outbox.outboxstarter.component;

import com.ginthub.ggruzdov.outbox.outboxstarter.model.Outbox;
import com.ginthub.ggruzdov.outbox.outboxstarter.repository.OutboxRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@ConditionalOnMissingBean(type = "OutboxCommiter")
public class BufferedOutboxCommiter implements OutboxCommiter {

    private final Integer bufferSize;
    private final BlockingQueue<UUID> processedEventsQueue;
    private final OutboxRepository outboxRepository;

    public BufferedOutboxCommiter(
        @Value("${outbox.buffer.size:1}") Integer bufferSize,
        OutboxRepository outboxRepository,
        MeterRegistry meterRegistry
    ) {
        this.bufferSize = bufferSize;
        this.outboxRepository = outboxRepository;
        this.processedEventsQueue = new LinkedBlockingQueue<>();
        Gauge.builder("outbox_buffer_size", processedEventsQueue, BlockingQueue::size)
            .description("Amount of outboxes awaiting commit")
            .register(meterRegistry);
    }

    @Override
    public void commit(Outbox outbox) {
        processedEventsQueue.add(outbox.getId());
        if (processedEventsQueue.size() >= bufferSize) {
            flush();
        }
    }

    // Here we do not use ShedLock because the queue is local for each application instance
    @Scheduled(fixedDelayString = "${outbox.buffer.autoFlushDelay:PT5S}")
    void flush() {
        if (!processedEventsQueue.isEmpty()) {
            var ids = new ArrayList<UUID>(processedEventsQueue.size());
            processedEventsQueue.drainTo(ids);
            try {
                var updated = outboxRepository.setProcessedAt(Instant.now(), ids);
                log.info("Commited {} outboxes", updated);
            } catch (Exception ex) {
                log.error("Failed to commit outboxes", ex);
                processedEventsQueue.addAll(ids);
            }
        }
    }

    @PreDestroy
    void tearDown() {
        flush();
    }
}
