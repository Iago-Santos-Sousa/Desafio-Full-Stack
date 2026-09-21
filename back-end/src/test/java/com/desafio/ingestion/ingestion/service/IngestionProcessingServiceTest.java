package com.desafio.ingestion.ingestion.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IngestionProcessingServiceTest {
  @Test
  void confirmsProcessingStateBeforeReturningJob() {
    IngestionJobRepository jobs = org.mockito.Mockito.mock(IngestionJobRepository.class);
    IngestionJob job = org.mockito.Mockito.mock(IngestionJob.class);
    UUID jobId = UUID.randomUUID();
    when(jobs.findById(jobId)).thenReturn(Optional.of(job));
    when(job.getStatus()).thenReturn(JobStatus.QUEUED);
    when(jobs.save(job)).thenReturn(job);

    new IngestionProcessingService(jobs).start(jobId);

    verify(job).processing();
    verify(jobs).save(job);
  }

  @Test
  void doesNotRewriteAlreadyProcessingJob() {
    IngestionJobRepository jobs = org.mockito.Mockito.mock(IngestionJobRepository.class);
    IngestionJob job = org.mockito.Mockito.mock(IngestionJob.class);
    UUID jobId = UUID.randomUUID();
    when(jobs.findById(jobId)).thenReturn(Optional.of(job));
    when(job.getStatus()).thenReturn(JobStatus.PROCESSING);

    new IngestionProcessingService(jobs).start(jobId);

    verify(job, never()).processing();
    verify(jobs, never()).save(any(IngestionJob.class));
  }

  @Test
  void rejectsTerminalJob() {
    IngestionJobRepository jobs = org.mockito.Mockito.mock(IngestionJobRepository.class);
    IngestionJob job = org.mockito.Mockito.mock(IngestionJob.class);
    UUID jobId = UUID.randomUUID();
    when(jobs.findById(jobId)).thenReturn(Optional.of(job));
    when(job.getStatus()).thenReturn(JobStatus.COMPLETED);

    assertThatThrownBy(() -> new IngestionProcessingService(jobs).start(jobId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Ingestion job is not ready for processing");
  }
}
