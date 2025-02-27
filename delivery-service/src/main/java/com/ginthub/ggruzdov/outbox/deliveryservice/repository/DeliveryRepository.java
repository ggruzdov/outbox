package com.ginthub.ggruzdov.outbox.deliveryservice.repository;

import com.ginthub.ggruzdov.outbox.deliveryservice.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    Delivery findByOrderId(Integer orderId);
}