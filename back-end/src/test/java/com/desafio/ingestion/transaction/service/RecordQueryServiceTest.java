package com.desafio.ingestion.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.desafio.ingestion.ingestion.domain.JobProgress;
import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import com.desafio.ingestion.transaction.dto.RecordPageResponse;
import com.desafio.ingestion.transaction.repository.RecordPageRow;
import com.desafio.ingestion.transaction.repository.RecordRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecordQueryServiceTest {
  @Test
  void capsPageSizeAndBuildsNextCursorFromExtraRow() {
    RecordRepository records = mock(RecordRepository.class);
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    UUID jobId = UUID.randomUUID();

    IngestionJob job = new IngestionJob(jobId, "data.csv", "/tmp/data.csv", 10, List.of("name"));
    job.updateProgress(new JobProgress(3, 3, 0));
    when(jobs.findById(jobId)).thenReturn(Optional.of(job));

    when(records.findPage(2, 10L, jobId))
        .thenReturn(
            List.of(
                new RecordPageRow(11L, 11, Map.of("name", "Ada")),
                new RecordPageRow(12L, 12, Map.of("name", "Bob")),
                new RecordPageRow(13L, 13, Map.of("name", "Cleo"))));

    RecordPageResponse response = new RecordQueryService(records, jobs, 1_000).list(jobId, 2, 10L);

    assertThat(response.columns()).containsExactly("name");
    assertThat(response.items()).hasSize(2);
    assertThat(response.items().get(0).values()).containsEntry("name", "Ada");
    assertThat(response.nextCursor()).isEqualTo(12L);
    assertThat(response.totalRecords()).isEqualTo(3);
    assertThat(response.totalPages()).isEqualTo(2);
    verify(records).findPage(2, 10L, jobId);
  }

  @Test
  void returnsSinglePageWhenRepositoryHasNoExtraRow() {
    RecordRepository records = mock(RecordRepository.class);
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    UUID jobId = UUID.randomUUID();

    IngestionJob job = new IngestionJob(jobId, "data.csv", "/tmp/data.csv", 10, List.of("name"));
    job.updateProgress(new JobProgress(1, 1, 0));
    when(jobs.findById(jobId)).thenReturn(Optional.of(job));

    when(records.findPage(1, null, jobId))
        .thenReturn(List.of(new RecordPageRow(1L, 1, Map.of("name", "Ada"))));

    RecordPageResponse response = new RecordQueryService(records, jobs, 1_000).list(jobId, 0, null);

    assertThat(response.items()).hasSize(1);
    assertThat(response.nextCursor()).isNull();
    assertThat(response.totalRecords()).isEqualTo(1);
    assertThat(response.totalPages()).isEqualTo(1);
    verify(records).findPage(1, null, jobId);
  }

  @Test
  void returnsZeroPagesWhenJobHasNoValidRecords() {
    RecordRepository records = mock(RecordRepository.class);
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    UUID jobId = UUID.randomUUID();

    IngestionJob job = new IngestionJob(jobId, "data.csv", "/tmp/data.csv", 10, List.of("name"));
    when(jobs.findById(jobId)).thenReturn(Optional.of(job));
    when(records.findPage(25, null, jobId)).thenReturn(List.of());

    RecordPageResponse response =
        new RecordQueryService(records, jobs, 1_000).list(jobId, 25, null);

    assertThat(response.items()).isEmpty();
    assertThat(response.totalRecords()).isZero();
    assertThat(response.totalPages()).isZero();
  }

  @Test
  void listsPersistedRecordsWhenJobFailedAfterPartialProcessing() {
    RecordRepository records = mock(RecordRepository.class);
    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    UUID jobId = UUID.randomUUID();

    IngestionJob job = new IngestionJob(jobId, "data.csv", "/tmp/data.csv", 10, List.of("name"));
    job.updateProgress(new JobProgress(5, 3, 2));
    job.fail("PROCESSING_TIMEOUT");
    when(jobs.findById(jobId)).thenReturn(Optional.of(job));
    when(records.findPage(25, null, jobId))
        .thenReturn(List.of(new RecordPageRow(1L, 2, Map.of("name", "Ada"))));

    RecordPageResponse response =
        new RecordQueryService(records, jobs, 1_000).list(jobId, 25, null);

    assertThat(response.items()).hasSize(1);
    assertThat(response.items().get(0).values()).containsEntry("name", "Ada");
    assertThat(response.totalRecords()).isEqualTo(3);
    assertThat(response.totalPages()).isEqualTo(1);
    verify(records).findPage(25, null, jobId);
  }
}
