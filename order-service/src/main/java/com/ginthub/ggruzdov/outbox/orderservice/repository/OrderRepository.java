package com.ginthub.ggruzdov.outbox.orderservice.repository;

import com.ginthub.ggruzdov.outbox.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}