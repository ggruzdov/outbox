package com.ginthub.ggruzdov.outbox.orderservice.component;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TraceContextUtil {

    private final Tracer tracer;

    public String getTraceparent() {
        var span = tracer.currentSpan();
        if (span == null) {
            log.warn("No trace parent");
            return null;
        }

        String traceId = span.context().traceId();
        String spanId = span.context().spanId();
        String traceFlags = Boolean.TRUE.equals(span.context().sampled()) ? "01" : "00";

        return String.format("00-%s-%s-%s", traceId, spanId, traceFlags);
    }

    public void executeInTraceContext(String traceparent, Runnable action) {
        if (traceparent == null) {
            log.warn("Traceparent is null, invoking action out of trace context");
            action.run();
            return;
        }

        String[] parts = traceparent.split("-");
        String traceId = parts[1];
        String spanId = parts[2];
        String traceFlags = parts[3];

        TraceContext context = tracer.traceContextBuilder()
            .traceId(traceId)
            .spanId(spanId)
            .sampled("01".equals(traceFlags))
            .build();

        Span newSpan = tracer.spanBuilder().setParent(context).name("restored-outbox-span").start();

        try (Tracer.SpanInScope ignored = tracer.withSpan(newSpan.start())) {
            action.run();
        } finally {
            newSpan.end();
        }
    }
}
