package com.desafio.ingestion.ingestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.desafio.ingestion.ingestion.entity.IngestionEventOutbox;
import com.desafio.ingestion.ingestion.entity.OutboxStatus;
import com.desafio.ingestion.ingestion.repository.IngestionEventOutboxRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class IngestionOutboxServiceTest {
  @Test
  void enqueuesAcceptedIngestionEvent() {
    IngestionEventOutboxRepository repository = mock(IngestionEventOutboxRepository.class);
    UUID jobId = UUID.randomUUID();

    new IngestionOutboxService(repository).enqueue(jobId);

    ArgumentCaptor<IngestionEventOutbox> captor =
        ArgumentCaptor.forClass(IngestionEventOutbox.class);
    verify(repository).save(captor.capture());
    IngestionEventOutbox event = captor.getValue();
    assertThat(event.getJobId()).isEqualTo(jobId);
    assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
    assertThat(event.getAttempts()).isZero();
  }

  @Test
  void outboxEventTransitionsToPublishedAndRetryStates() {
    IngestionEventOutbox event =
        new IngestionEventOutbox(UUID.randomUUID(), java.time.Instant.now());

    event.markPublished(java.time.Instant.now());
    assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    assertThat(event.getPublishedAt()).isNotNull();

    event.requeue(java.time.Instant.now());
    event.markRetry("broker unavailable", java.time.Instant.now());
    assertThat(event.getStatus()).isEqualTo(OutboxStatus.RETRY);
    assertThat(event.getAttempts()).isEqualTo(1);
  }
}
