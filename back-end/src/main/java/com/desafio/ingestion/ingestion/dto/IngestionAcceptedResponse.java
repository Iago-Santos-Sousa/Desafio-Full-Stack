package com.desafio.ingestion.ingestion.dto;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Confirmação do recebimento do arquivo para processamento assíncrono.")
public record IngestionAcceptedResponse(UUID jobId, JobStatus status, String statusUrl) {
  public static IngestionAcceptedResponse from(IngestionJob job) {
    return new IngestionAcceptedResponse(
        job.getId(), job.getStatus(), "/api/v1/ingestions/" + job.getId());
  }
}
