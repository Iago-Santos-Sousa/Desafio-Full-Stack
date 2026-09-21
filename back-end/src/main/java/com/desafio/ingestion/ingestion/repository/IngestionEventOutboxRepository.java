package com.desafio.ingestion.ingestion.repository;

import com.desafio.ingestion.ingestion.entity.IngestionEventOutbox;
import com.desafio.ingestion.ingestion.entity.OutboxStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionEventOutboxRepository extends JpaRepository<IngestionEventOutbox, UUID> {
  List<IngestionEventOutbox> findTop20ByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
      Collection<OutboxStatus> statuses, Instant now);

  Optional<IngestionEventOutbox> findByJobId(UUID jobId);
}
