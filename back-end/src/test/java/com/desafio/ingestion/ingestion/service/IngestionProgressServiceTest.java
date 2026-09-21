package com.desafio.ingestion.ingestion.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IngestionProgressServiceTest {
  @Test
  void persistsChunkCountersAsProcessing() {
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    UUID jobId = UUID.randomUUID();

    when(jobs.updateProgress(jobId, JobStatus.PROCESSING, 100, 96, 4)).thenReturn(1);

    new IngestionProgressService(jobs).update(jobId, 100, 96, 4);

    verify(jobs).updateProgress(jobId, JobStatus.PROCESSING, 100, 96, 4);
  }

  @Test
  void failsWhenCheckpointUpdatesNoProcessingJob() {
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    UUID jobId = UUID.randomUUID();

    when(jobs.updateProgress(jobId, JobStatus.PROCESSING, 100, 96, 4)).thenReturn(0);

    Assertions.assertThrows(
        IllegalStateException.class,
        () -> new IngestionProgressService(jobs).update(jobId, 100, 96, 4));
  }
}
