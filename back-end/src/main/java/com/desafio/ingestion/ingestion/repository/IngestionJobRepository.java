package com.desafio.ingestion.ingestion.repository;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IngestionJobRepository extends JpaRepository<IngestionJob, UUID> {
  List<IngestionJob> findByStatusInOrderByCreatedAtAsc(
      Collection<JobStatus> statuses, Pageable pageable);

  List<IngestionJob> findByStatusAndQueuedAtBefore(
      JobStatus status, Instant queuedAt, Pageable pageable);

  List<IngestionJob> findByStatusAndUpdatedAtBefore(
      JobStatus status, Instant updatedAt, Pageable pageable);

  @Modifying
  @Query(
      "update IngestionJob j set j.totalRows=:processed,"
          + " j.processedRows=:processed, j.validRows=:valid,"
          + " j.invalidRows=:invalid, j.updatedAt=CURRENT_TIMESTAMP"
          + " where j.id=:id and j.status=:status")
  int updateProgress(
      @Param("id") UUID id,
      @Param("status") JobStatus status,
      @Param("processed") long processed,
      @Param("valid") long valid,
      @Param("invalid") long invalid);
}
