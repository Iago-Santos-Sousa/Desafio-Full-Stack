package com.desafio.ingestion.ingestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ingestion_event_outbox")
public class IngestionEventOutbox {
  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private UUID jobId;

  @Column(nullable = false, length = 64)
  private String eventType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private OutboxStatus status;

  @Column(nullable = false)
  private int attempts;

  @Column(nullable = false)
  private Instant nextAttemptAt;

  @Column(length = 1000)
  private String lastError;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant publishedAt;

  protected IngestionEventOutbox() {}

  public IngestionEventOutbox(UUID jobId, Instant now) {
    this.id = jobId;
    this.jobId = jobId;
    this.eventType = "INGESTION_ACCEPTED";
    this.status = OutboxStatus.PENDING;
    this.nextAttemptAt = now;
    this.createdAt = now;
  }

  public UUID getId() {
    return id;
  }

  public UUID getJobId() {
    return jobId;
  }

  public OutboxStatus getStatus() {
    return status;
  }

  public int getAttempts() {
    return attempts;
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public String getLastError() {
    return lastError;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public void markPublished(Instant now) {
    status = OutboxStatus.PUBLISHED;
    publishedAt = now;
    lastError = null;
  }

  public void markRetry(String error, Instant nextAttempt) {
    attempts++;
    status = attempts >= 3 ? OutboxStatus.DEAD : OutboxStatus.RETRY;

    lastError =
        error == null
            ? "Message publish failed"
            : error.substring(0, Math.min(error.length(), 1000));

    nextAttemptAt = nextAttempt;
  }

  public void requeue(Instant now) {
    status = OutboxStatus.PENDING;
    attempts = 0;
    nextAttemptAt = now;
    lastError = null;
    publishedAt = null;
  }
}
