package com.ginthub.ggruzdov.outbox.orderservice.repository;

import com.ginthub.ggruzdov.outbox.orderservice.model.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface OrderOutboxRepository extends JpaRepository<OrderOutbox, UUID> {

    // We use interval to prevent message duplication for fresh records
    @Query(
        value = "select * from order_outbox oo where oo.processed_at is null and oo.created_at <= now() - interval '1 minutes'",
        nativeQuery = true
    )
    Stream<OrderOutbox> findAllUnprocessed();

    @Transactional
    @Query("update OrderOutbox oo set oo.processedAt = :dt where oo.id in :ids")
    @Modifying
    int setProcessedAt(Instant dt, List<UUID> ids);
}