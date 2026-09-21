package com.desafio.ingestion.ingestion.batch;

import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import com.desafio.ingestion.ingestion.service.IngestionProgressService;
import java.util.UUID;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

@Component
public class IngestionProgressChunkListener
    implements ChunkListener<DynamicCsvRow, DynamicCsvRow>,
        SkipListener<DynamicCsvRow, DynamicCsvRow>,
        StepExecutionListener {
  private final IngestionProgressService progress;
  private final IngestionJobRepository jobs;
  private final IngestionProgressTracker tracker;

  public IngestionProgressChunkListener(
      IngestionProgressService progress,
      IngestionJobRepository jobs,
      IngestionProgressTracker tracker) {
    this.progress = progress;
    this.jobs = jobs;
    this.tracker = tracker;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    UUID jobId = UUID.fromString(stepExecution.getJobParameters().getString("jobId"));
    jobs.findById(jobId)
        .ifPresent(
            job ->
                tracker.initialize(
                    jobId, job.getProcessedRows(), job.getValidRows(), job.getInvalidRows()));
  }

  @Override
  public void beforeChunk(Chunk<DynamicCsvRow> chunk) {
    tracker.beginChunk();
  }

  @Override
  public void afterChunk(Chunk<DynamicCsvRow> chunk) {
    if (!tracker.isCheckpointed() && tracker.hasChanges()) {
      progress.update(tracker.jobId(), tracker.snapshot());
      tracker.markCheckpointed();
    }
  }

  @Override
  public void onChunkError(Exception exception, Chunk<DynamicCsvRow> chunk) {
    tracker.rollbackChunk();
  }

  @Override
  public void onSkipInRead(Throwable throwable) {
    tracker.recordInvalid();
  }

  @Override
  public void onSkipInProcess(DynamicCsvRow item, Throwable throwable) {
    tracker.recordInvalid();
  }

  @Override
  public void onSkipInWrite(DynamicCsvRow item, Throwable throwable) {
    tracker.recordInvalid();
  }
}
