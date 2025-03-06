package com.ginthub.ggruzdov.outbox.orderservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
    @NotBlank
    String requestId,
    @NotNull
    Integer customerId,
    @NotNull
    Integer totalPrice
) {
}
