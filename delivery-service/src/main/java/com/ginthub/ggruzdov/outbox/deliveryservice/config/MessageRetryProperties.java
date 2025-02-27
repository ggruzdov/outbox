package com.ginthub.ggruzdov.outbox.deliveryservice.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "messaging.retry")
public record MessageRetryProperties(
    @NotNull
    Long initInterval,
    @NotNull
    Long maxInterval,
    @NotNull
    Float multiplier
) {
}
