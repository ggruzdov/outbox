package com.ginthub.ggruzdov.outbox.orderservice.component;

import com.ginthub.ggruzdov.outbox.orderservice.model.OrderOutbox;

public interface MessageCommiter {

    void commit(OrderOutbox orderOutbox);
}
