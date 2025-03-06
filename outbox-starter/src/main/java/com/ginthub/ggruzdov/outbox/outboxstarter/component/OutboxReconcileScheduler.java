package com.ginthub.ggruzdov.outbox.outboxstarter.component;


import com.ginthub.ggruzdov.outbox.outboxstarter.model.Outbox;
import com.ginthub.ggruzdov.outbox.outboxstarter.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxReconcileScheduler {

    private final OutboxSender outboxSender;
    private final TraceContextUtil traceContextUtil;
    private final OutboxRepository outboxRepository;

    @Transactional
    @Scheduled(fixedDelayString = "${outbox.reconcile.interval:PT3M}")
    @SchedulerLock(
        name = "outbox_reconcile",
        lockAtLeastFor = "${outbox.reconcile.lockAtLeastFor}",
        lockAtMostFor = "${outbox.reconcile.lockAtMostFor}"
    )
    public void reconcile() {
        log.info("Reconciling orders");
        var count = new AtomicInteger();
        try(Stream<Outbox> orderOutboxStream = outboxRepository.findAllUnprocessed()) {
            orderOutboxStream.forEach(orderOutbox -> {
                traceContextUtil.executeInTraceContext(orderOutbox.getTraceparent(), () -> outboxSender.send(orderOutbox));
                count.getAndIncrement();
            });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            log.info("Reconciled {} orders", count);
        }
    }
}
