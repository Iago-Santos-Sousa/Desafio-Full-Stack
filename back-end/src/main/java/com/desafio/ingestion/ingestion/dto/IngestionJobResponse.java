package com.desafio.ingestion.ingestion.dto;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Detalhes e progresso de uma ingestão.")
public record IngestionJobResponse(
    UUID jobId,
    String originalFilename,
    JobStatus status,
    long totalRows,
    long processedRows,
    long validRows,
    long invalidRows,
    List<String> columns,
    long fileSizeBytes,
    Instant createdAt,
    Instant queuedAt,
    Instant startedAt,
    Instant finishedAt,
    Instant updatedAt,
    String errorSummary) {
  public static IngestionJobResponse from(IngestionJob job) {
    return new IngestionJobResponse(
        job.getId(),
        job.getOriginalFilename(),
        job.getStatus(),
        job.getTotalRows(),
        job.getProcessedRows(),
        job.getValidRows(),
        job.getInvalidRows(),
        job.getColumns(),
        job.getFileSizeBytes(),
        job.getCreatedAt(),
        job.getQueuedAt(),
        job.getStartedAt(),
        job.getFinishedAt(),
        job.getUpdatedAt(),
        job.getErrorSummary());
  }
}
