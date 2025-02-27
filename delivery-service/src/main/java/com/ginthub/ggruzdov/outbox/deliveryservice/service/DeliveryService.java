package com.ginthub.ggruzdov.outbox.deliveryservice.service;

import com.ginthub.ggruzdov.outbox.deliveryservice.messaging.event.OrderCreatedEvent;
import com.ginthub.ggruzdov.outbox.deliveryservice.model.Delivery;
import com.ginthub.ggruzdov.outbox.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final Random random = new Random();
    private final DeliveryRepository deliveryRepository;

    public Delivery findByOrderId(Integer orderId) {
        return deliveryRepository.findByOrderId(orderId);
    }

    @Transactional
    public Delivery create(OrderCreatedEvent orderCreatedEvent) {
        // Exception emulation to check message retry correctness
        if (random.nextInt(1, 8) != 4) {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Third party integration failed");
        }
        var delivery = Delivery.builder()
            .orderId(orderCreatedEvent.id())
            .status("NEW")
            .deliveryDate(Instant.now().plus(1L, ChronoUnit.DAYS))
            .build();

        return deliveryRepository.save(delivery);
    }
}
