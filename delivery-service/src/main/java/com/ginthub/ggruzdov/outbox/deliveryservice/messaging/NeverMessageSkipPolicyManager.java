package com.ginthub.ggruzdov.outbox.deliveryservice.messaging;

import com.ginthub.ggruzdov.outbox.deliveryservice.messaging.event.Outbox;
import org.springframework.stereotype.Component;

@Component
public class NeverMessageSkipPolicyManager implements MessageSkipPolicyManager {

    @Override
    public boolean shouldSkip(Outbox outbox) {
        // Here we can add some third party integration, like Redis, and check some key.
        // For example, get value by key "message:skip:{outbox.id}" and if it is not empty return true to skip the message.
        return false;
    }
}
