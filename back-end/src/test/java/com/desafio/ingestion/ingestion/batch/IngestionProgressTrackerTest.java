package com.desafio.ingestion.ingestion.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import com.desafio.ingestion.ingestion.service.IngestionProgressService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;

class IngestionProgressTrackerTest {
  @Test
  void restoresCountersWhenChunkRollsBack() {
    UUID jobId = UUID.randomUUID();
    IngestionProgressTracker tracker = new IngestionProgressTracker();
    tracker.initialize(jobId, 10, 8, 2);
    tracker.beginChunk();

    tracker.recordRead();
    tracker.recordValid(1);
    tracker.recordInvalid();
    tracker.rollbackChunk();

    assertThat(tracker.jobId()).isEqualTo(jobId);
    assertThat(tracker.snapshot().processedRows()).isEqualTo(10);
    assertThat(tracker.snapshot().validRows()).isEqualTo(8);
    assertThat(tracker.snapshot().invalidRows()).isEqualTo(2);
  }

  @Test
  void tracksReadValidAndInvalidRowsInOneSnapshot() {
    IngestionProgressTracker tracker = new IngestionProgressTracker();
    tracker.initialize(UUID.randomUUID(), 0, 0, 0);
    tracker.beginChunk();

    tracker.recordRead();
    tracker.recordRead();
    tracker.recordValid(1);
    tracker.recordInvalid();

    assertThat(tracker.snapshot().processedRows()).isEqualTo(2);
    assertThat(tracker.snapshot().validRows()).isEqualTo(1);
    assertThat(tracker.snapshot().invalidRows()).isEqualTo(1);
    assertThat(tracker.hasChanges()).isTrue();
  }

  @Test
  void initializesFromStepExecutionParameters() {
    UUID jobId = UUID.randomUUID();
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    IngestionProgressService progress = mock(IngestionProgressService.class);
    IngestionJob job = mock(IngestionJob.class);
    when(jobs.findById(jobId)).thenReturn(java.util.Optional.of(job));
    when(job.getProcessedRows()).thenReturn(10L);
    when(job.getValidRows()).thenReturn(8L);
    when(job.getInvalidRows()).thenReturn(2L);
    IngestionProgressTracker tracker = new IngestionProgressTracker();

    IngestionProgressChunkListener listener =
        new IngestionProgressChunkListener(progress, jobs, tracker);
    JobExecution execution =
        new JobExecution(
            1,
            new JobInstance(1, "ingestionJob"),
            new JobParametersBuilder().addString("jobId", jobId.toString()).toJobParameters());

    listener.beforeStep(new StepExecution(1, "ingestionStep", execution));

    assertThat(tracker.jobId()).isEqualTo(jobId);
    assertThat(tracker.snapshot().processedRows()).isEqualTo(10L);
    assertThat(tracker.snapshot().validRows()).isEqualTo(8L);
    assertThat(tracker.snapshot().invalidRows()).isEqualTo(2L);
  }
}
