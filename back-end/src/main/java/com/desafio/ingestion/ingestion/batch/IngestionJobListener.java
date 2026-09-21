package com.desafio.ingestion.ingestion.batch;

import com.desafio.ingestion.analytics.service.AnalyticsService;
import com.desafio.ingestion.ingestion.domain.IngestionOutcomeCalculator;
import com.desafio.ingestion.ingestion.domain.JobOutcome;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IngestionJobListener implements JobExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestionJobListener.class);
  private final IngestionJobRepository jobs;
  private final AnalyticsService analytics;

  public IngestionJobListener(IngestionJobRepository jobs, AnalyticsService analytics) {
    this.jobs = jobs;
    this.analytics = analytics;
  }

  @Override
  @Transactional
  public void beforeJob(JobExecution e) {
    jobs.findById(UUID.fromString(e.getJobParameters().getString("jobId")))
        .ifPresent(
            j -> {
              if (j.getStatus() != JobStatus.PROCESSING) {
                j.processing();
                jobs.save(j);
              }
            });
  }

  @Override
  @Transactional
  public void afterJob(JobExecution e) {
    UUID id = UUID.fromString(e.getJobParameters().getString("jobId"));
    long valid = e.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
    long invalid = e.getStepExecutions().stream().mapToLong(StepExecution::getSkipCount).sum();

    JobOutcome outcome =
        IngestionOutcomeCalculator.calculate(
            e.getStatus() == BatchStatus.COMPLETED, valid, invalid);

    if (outcome.status() == JobStatus.FAILED) {
      e.getAllFailureExceptions().forEach(ex -> LOGGER.error("Batch failed for jobId={}", id, ex));
    }

    Instant finishedAt = Instant.now();

    jobs.findById(id)
        .ifPresent(
            j -> {
              j.finish(
                  outcome.status(),
                  outcome.progress().processedRows(),
                  outcome.progress().validRows(),
                  outcome.progress().invalidRows(),
                  outcome.errorSummary());
              jobs.save(j);
            });

    try {
      analytics.refreshMetric(
          id, outcome.progress().processedRows(), outcome.progress().validRows(), finishedAt);
    } catch (RuntimeException ex) {
      LOGGER.error("Metric refresh failed for jobId={}", id, ex);
    }
  }
}
