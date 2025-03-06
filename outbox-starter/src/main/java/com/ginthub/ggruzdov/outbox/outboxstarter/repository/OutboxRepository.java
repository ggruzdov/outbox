package com.ginthub.ggruzdov.outbox.outboxstarter.repository;

import com.ginthub.ggruzdov.outbox.outboxstarter.model.Outbox;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class OutboxRepository {

    private final String selectAllUnprocessed;
    private final String insertOne;
    private final String updateAllIn;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public OutboxRepository(
        @Value("${outbox.table-name:outbox}") String outboxTableName,
        NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;

        this.selectAllUnprocessed = """
            select * from %s oo where oo.processed_at is null and oo.created_at <= now() - interval '1 minutes'
        """.formatted(outboxTableName);

        this.insertOne = """
            insert into %s(id, aggregate_id, event_type, traceparent, payload, created_at)
            values(:id, :aggregateId, :eventType, :traceparent, :payload, :createdAt)
        """.formatted(outboxTableName);

        this.updateAllIn = """
            update %s set processed_at = :dt where id in (:ids)
        """.formatted(outboxTableName);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Stream<Outbox> findAllUnprocessed() {
        return namedParameterJdbcTemplate.queryForStream(selectAllUnprocessed, Map.of(), (rs, rowNum) -> Outbox.builder()
            .id(UUID.fromString(rs.getString("id")))
            .aggregateId(rs.getString("aggregate_id"))
            .eventType(rs.getString("event_type"))
            .traceparent(rs.getString("traceparent"))
            .payload(rs.getString("payload"))
            .createdAt(rs.getTimestamp("created_at").toInstant())
            .build()
        );
    }

    public void save(Outbox outbox) {
        var parameters = new MapSqlParameterSource();
        parameters.addValue("id", outbox.getId());
        parameters.addValue("aggregateId", outbox.getAggregateId());
        parameters.addValue("eventType", outbox.getEventType());
        parameters.addValue("traceparent", outbox.getTraceparent());
        parameters.addValue("payload", toJsonB(outbox.getPayload()));
        parameters.addValue("createdAt", Timestamp.from(outbox.getCreatedAt()));

        namedParameterJdbcTemplate.update(insertOne, parameters);
    }

    public int setProcessedAt(Instant dt, List<UUID> ids) {
        var parameters = new MapSqlParameterSource();
        parameters.addValue("dt", Timestamp.from(dt));
        parameters.addValue("ids", ids);

        return namedParameterJdbcTemplate.update(updateAllIn, parameters);
    }

    private PGobject toJsonB(String json) {
        if (json == null) return null;
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(json);
            return jsonObject;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error converting String to JSONB", e);
        }
    }
}
