package com.desafio.ingestion.ingestion.service;

import com.desafio.ingestion.ingestion.domain.JobProgress;
import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.entity.OutboxStatus;
import com.desafio.ingestion.ingestion.repository.BatchProgressRepository;
import com.desafio.ingestion.ingestion.repository.IngestionEventOutboxRepository;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionRecoveryService {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestionRecoveryService.class);
  private final IngestionJobRepository jobs;
  private final IngestionEventOutboxRepository outbox;
  private final BatchProgressRepository batchProgress;
  private final Duration queueTimeout;

  public IngestionRecoveryService(
      IngestionJobRepository jobs,
      IngestionEventOutboxRepository outbox,
      BatchProgressRepository batchProgress,
      @Value("${app.messaging.queue-timeout-minutes:10}") long timeoutMinutes) {
    this.jobs = jobs;
    this.outbox = outbox;
    this.batchProgress = batchProgress;
    this.queueTimeout = Duration.ofMinutes(timeoutMinutes);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void fail(UUID jobId, String reason) {
    jobs.findById(jobId).ifPresent(job -> markFailed(job, reason));
  }

  @Transactional
  @Scheduled(fixedDelayString = "${app.messaging.recovery-interval-ms:60000}")
  public void reconcileStaleJobs() {
    Instant cutoff = Instant.now().minus(queueTimeout);

    List<IngestionJob> stale =
        jobs.findByStatusAndQueuedAtBefore(JobStatus.QUEUED, cutoff, PageRequest.of(0, 50));

    for (IngestionJob job : stale) {
      outbox
          .findByJobId(job.getId())
          .ifPresentOrElse(
              event -> {
                if (event.getStatus() == OutboxStatus.PUBLISHED
                    || event.getStatus() == OutboxStatus.DEAD) {
                  markFailed(job, "MESSAGE_DELIVERY_TIMEOUT");
                }
              },
              () -> markFailed(job, "MESSAGE_DELIVERY_TIMEOUT"));
    }

    List<IngestionJob> abandoned =
        jobs.findByStatusAndUpdatedAtBefore(JobStatus.PROCESSING, cutoff, PageRequest.of(0, 50));

    for (IngestionJob job : abandoned) {
      reconcileProgress(job);
      markFailed(job, "PROCESSING_TIMEOUT");
    }
  }

  private void reconcileProgress(IngestionJob job) {
    batchProgress
        .findLatest(job.getId())
        .ifPresentOrElse(
            progress -> {
              if (isAhead(job, progress)) {
                job.updateProgress(progress);
                LOGGER.info(
                    "event=job_progress_reconciled jobId={} processedRows={} validRows={}"
                        + " invalidRows={}",
                    job.getId(),
                    progress.processedRows(),
                    progress.validRows(),
                    progress.invalidRows());
              }
            },
            () ->
                LOGGER.warn(
                    "event=job_progress_recovery_incomplete jobId={}"
                        + " reason=BATCH_STEP_EXECUTION_NOT_FOUND",
                    job.getId()));
  }

  private boolean isAhead(IngestionJob job, JobProgress progress) {
    return progress.processedRows() >= job.getProcessedRows()
        && progress.validRows() >= job.getValidRows()
        && progress.invalidRows() >= job.getInvalidRows()
        && (progress.processedRows() > job.getProcessedRows()
            || progress.validRows() > job.getValidRows()
            || progress.invalidRows() > job.getInvalidRows());
  }

  private void markFailed(IngestionJob job, String reason) {
    if (job.getStatus() == JobStatus.COMPLETED
        || job.getStatus() == JobStatus.COMPLETED_WITH_ERRORS
        || job.getStatus() == JobStatus.FAILED) {
      return;
    }

    job.fail(reason);
    jobs.save(job);
    LOGGER.error("event=job_failed jobId={} reason={}", job.getId(), reason);
  }
}
