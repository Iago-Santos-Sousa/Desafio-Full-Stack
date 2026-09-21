package com.desafio.ingestion.ingestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.desafio.ingestion.ingestion.dto.IngestionJobListItem;
import com.desafio.ingestion.ingestion.dto.IngestionJobPageResponse;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.repository.IngestionJobQueryRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IngestionQueryServiceTest {
  @Test
  void limitsPageAndCreatesCursorFromLastReturnedJob() {
    IngestionJobQueryRepository repository = mock(IngestionJobQueryRepository.class);
    Instant first = Instant.parse("2026-01-03T00:00:00Z");
    Instant second = Instant.parse("2026-01-02T00:00:00Z");
    IngestionJobListItem firstItem = item(first);
    IngestionJobListItem secondItem = item(second);
    IngestionJobListItem extraItem = item(Instant.parse("2026-01-01T00:00:00Z"));
    when(repository.findPage(2, null)).thenReturn(List.of(firstItem, secondItem, extraItem));

    IngestionJobPageResponse response = new IngestionQueryService(repository).list(2, null);

    assertThat(response.items()).containsExactly(firstItem, secondItem);
    assertThat(response.nextCursor()).isNotBlank();
    verify(repository).findPage(2, null);
  }

  @Test
  void clampsRequestedPageSizeToSafeBounds() {
    IngestionJobQueryRepository repository = mock(IngestionJobQueryRepository.class);
    when(repository.findPage(1, null)).thenReturn(List.of());
    when(repository.findPage(50, null)).thenReturn(List.of());
    IngestionQueryService service = new IngestionQueryService(repository);

    service.list(0, null);
    service.list(500, null);

    verify(repository).findPage(eq(1), isNull());
    verify(repository).findPage(eq(50), isNull());
  }

  private IngestionJobListItem item(Instant createdAt) {
    return new IngestionJobListItem(
        UUID.randomUUID(), "data.csv", 10, JobStatus.COMPLETED, createdAt, createdAt);
  }
}
