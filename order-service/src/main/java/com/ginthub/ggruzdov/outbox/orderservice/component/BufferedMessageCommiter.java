package com.ginthub.ggruzdov.outbox.orderservice.component;

import com.ginthub.ggruzdov.outbox.orderservice.model.OrderOutbox;
import com.ginthub.ggruzdov.outbox.orderservice.repository.OrderOutboxRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class BufferedMessageCommiter implements MessageCommiter {

    @Value("${outbox.commit.size:1}")
    private Integer commitSize;

    // Add monitoring
    private final BlockingQueue<UUID> processedEventsQueue = new LinkedBlockingQueue<>();
    private final OrderOutboxRepository outboxRepository;

    @Override
    public void commit(OrderOutbox orderOutbox) {
        processedEventsQueue.add(orderOutbox.getId());
        if (processedEventsQueue.size() >= commitSize) {
            flush();
        }
    }

    @Scheduled(fixedDelayString = "${outbox.commit.autoFlushDelay}")
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
