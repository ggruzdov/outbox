package com.ginthub.ggruzdov.outbox.orderservice.component;

import com.ginthub.ggruzdov.outbox.orderservice.model.OrderOutbox;
import com.ginthub.ggruzdov.outbox.orderservice.repository.OrderOutboxRepository;
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

    private final MessageSender messageSender;
    private final TraceContextUtil traceContextUtil;
    private final OrderOutboxRepository orderOutboxRepository;

    @Transactional
    @Scheduled(fixedDelayString = "${outbox.reconcile.cron}")
    @SchedulerLock(
        name = "outbox_reconcile",
        lockAtLeastFor = "${outbox.reconcile.lockAtLeastFor}",
        lockAtMostFor = "${outbox.reconcile.lockAtMostFor}"
    )
    public void reconcile() {
        log.info("Reconciling orders");
        var count = new AtomicInteger();
        try(Stream<OrderOutbox> orderOutboxStream = orderOutboxRepository.findAllUnprocessed()) {
            orderOutboxStream.forEach(orderOutbox -> {
                traceContextUtil.executeInTraceContext(orderOutbox.getTraceparent(), () -> messageSender.send(orderOutbox));
                count.getAndIncrement();
            });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            log.info("Reconciled {} orders", count);
        }
    }
}
