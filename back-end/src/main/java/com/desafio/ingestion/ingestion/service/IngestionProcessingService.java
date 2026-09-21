package com.desafio.ingestion.ingestion.service;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionProcessingService {
  private final IngestionJobRepository jobs;

  public IngestionProcessingService(IngestionJobRepository jobs) {
    this.jobs = jobs;
  }

  @Transactional
  public IngestionJob start(UUID jobId) {
    IngestionJob job =
        jobs.findById(jobId)
            .orElseThrow(() -> new IllegalStateException("Ingestion job not found"));

    if (job.getStatus() == JobStatus.PROCESSING) {
      return job;
    }

    if (job.getStatus() != JobStatus.QUEUED && job.getStatus() != JobStatus.RECEIVED) {
      throw new IllegalStateException("Ingestion job is not ready for processing");
    }

    job.processing();
    return jobs.save(job);
  }
}
