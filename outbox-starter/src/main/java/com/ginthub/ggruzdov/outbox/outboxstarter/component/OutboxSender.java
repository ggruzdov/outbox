package com.ginthub.ggruzdov.outbox.outboxstarter.component;

import com.ginthub.ggruzdov.outbox.outboxstarter.model.Outbox;

public interface OutboxSender {

    void send(Outbox outbox);
}
