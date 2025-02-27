package com.ginthub.ggruzdov.outbox.orderservice.request;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
    @NotNull
    Integer customerId,
    @NotNull
    Integer totalPrice
) {
}
