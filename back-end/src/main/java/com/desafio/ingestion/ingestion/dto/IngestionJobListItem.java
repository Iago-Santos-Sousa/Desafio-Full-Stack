package com.desafio.ingestion.ingestion.dto;

import com.desafio.ingestion.ingestion.entity.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Resumo de um job usado na paginação de ingestões.")
public record IngestionJobListItem(
    UUID jobId,
    String originalFilename,
    long fileSizeBytes,
    JobStatus status,
    Instant createdAt,
    Instant updatedAt) {}
