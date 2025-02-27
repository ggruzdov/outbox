package com.ginthub.ggruzdov.outbox.deliveryservice.messaging;

import com.ginthub.ggruzdov.outbox.deliveryservice.messaging.event.Outbox;

public interface MessageSkipPolicyManager {

    boolean shouldSkip(Outbox outbox);
}
