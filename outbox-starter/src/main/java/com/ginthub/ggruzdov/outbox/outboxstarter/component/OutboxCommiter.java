package com.ginthub.ggruzdov.outbox.outboxstarter.component;

import com.ginthub.ggruzdov.outbox.outboxstarter.model.Outbox;

public interface OutboxCommiter {

    void commit(Outbox outbox);
}
