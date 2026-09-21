package com.desafio.ingestion.ingestion.service;

import com.desafio.ingestion.ingestion.domain.JobProgress;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionProgressService {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestionProgressService.class);
  private final IngestionJobRepository jobs;

  public IngestionProgressService(IngestionJobRepository jobs) {
    this.jobs = jobs;
  }

  @Transactional
  public void update(UUID jobId, long processed, long valid, long invalid) {
    JobProgress progress = new JobProgress(processed, valid, invalid);
    update(jobId, progress);
  }

  @Transactional
  public void update(UUID jobId, JobProgress progress) {

    int affected =
        jobs.updateProgress(
            jobId,
            JobStatus.PROCESSING,
            progress.processedRows(),
            progress.validRows(),
            progress.invalidRows());

    if (affected != 1) {
      LOGGER.error(
          "event=progress_checkpoint_failed jobId={} processedRows={} validRows={} invalidRows={}"
              + " affectedRows={}",
          jobId,
          progress.processedRows(),
          progress.validRows(),
          progress.invalidRows(),
          affected);

      throw new IllegalStateException("Could not update ingestion progress");
    }

    LOGGER.debug(
        "event=progress_checkpoint jobId={} processedRows={} validRows={} invalidRows={}"
            + " affectedRows={}",
        jobId,
        progress.processedRows(),
        progress.validRows(),
        progress.invalidRows(),
        affected);
  }
}
