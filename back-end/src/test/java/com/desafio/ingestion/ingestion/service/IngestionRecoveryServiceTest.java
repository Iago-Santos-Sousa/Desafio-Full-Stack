package com.desafio.ingestion.ingestion.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.desafio.ingestion.ingestion.domain.JobProgress;
import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.repository.BatchProgressRepository;
import com.desafio.ingestion.ingestion.repository.IngestionEventOutboxRepository;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IngestionRecoveryServiceTest {
  @Test
  void failsStaleQueuedJobWithoutOutboxEvent() {
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    IngestionEventOutboxRepository outbox = mock(IngestionEventOutboxRepository.class);
    BatchProgressRepository batchProgress = mock(BatchProgressRepository.class);
    IngestionJob job = mock(IngestionJob.class);
    UUID jobId = UUID.randomUUID();
    when(job.getId()).thenReturn(jobId);
    when(job.getStatus()).thenReturn(JobStatus.QUEUED);

    when(jobs.findByStatusAndQueuedAtBefore(eq(JobStatus.QUEUED), any(), any()))
        .thenReturn(List.of(job));

    when(outbox.findByJobId(jobId)).thenReturn(Optional.empty());

    new IngestionRecoveryService(jobs, outbox, batchProgress, 1).reconcileStaleJobs();

    verify(job).fail("MESSAGE_DELIVERY_TIMEOUT");
    verify(jobs).save(job);
  }

  @Test
  void doesNotChangeTerminalJob() {
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    IngestionEventOutboxRepository outbox = mock(IngestionEventOutboxRepository.class);
    BatchProgressRepository batchProgress = mock(BatchProgressRepository.class);
    IngestionJob job = mock(IngestionJob.class);
    when(job.getId()).thenReturn(UUID.randomUUID());
    when(job.getStatus()).thenReturn(JobStatus.COMPLETED);
    when(jobs.findById(any())).thenReturn(Optional.of(job));

    new IngestionRecoveryService(jobs, outbox, batchProgress, 1).fail(job.getId(), "late failure");

    verify(job, never()).fail(anyString());
    verify(jobs, never()).save(any(IngestionJob.class));
  }

  @Test
  void failsAbandonedProcessingJobAndPreservesCheckpoint() {
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    IngestionEventOutboxRepository outbox = mock(IngestionEventOutboxRepository.class);
    BatchProgressRepository batchProgress = mock(BatchProgressRepository.class);
    IngestionJob job = mock(IngestionJob.class);
    UUID jobId = UUID.randomUUID();
    when(job.getId()).thenReturn(jobId);
    when(job.getStatus()).thenReturn(JobStatus.PROCESSING);
    when(job.getProcessedRows()).thenReturn(0L);
    when(job.getValidRows()).thenReturn(0L);
    when(job.getInvalidRows()).thenReturn(0L);
    when(jobs.findByStatusAndUpdatedAtBefore(eq(JobStatus.PROCESSING), any(), any()))
        .thenReturn(List.of(job));

    when(batchProgress.findLatest(jobId)).thenReturn(Optional.of(new JobProgress(100, 96, 4)));

    new IngestionRecoveryService(jobs, outbox, batchProgress, 1).reconcileStaleJobs();

    verify(job).updateProgress(new JobProgress(100, 96, 4));
    verify(job).fail("PROCESSING_TIMEOUT");
    verify(jobs).save(job);
  }
}
