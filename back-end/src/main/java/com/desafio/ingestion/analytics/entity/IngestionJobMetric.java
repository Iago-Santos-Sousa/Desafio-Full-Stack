package com.desafio.ingestion.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ingestion_job_metric")
public class IngestionJobMetric {
  @Id
  @Column(name = "ingestion_job_id")
  private UUID ingestionJobId;

  @Column(name = "finished_day", nullable = false)
  private LocalDate finishedDay;

  @Column(name = "processed_rows", nullable = false)
  private long processedRows;

  @Column(name = "valid_rows", nullable = false)
  private long validRows;

  protected IngestionJobMetric() {}

  public IngestionJobMetric(
      UUID ingestionJobId, LocalDate finishedDay, long processedRows, long validRows) {
    this.ingestionJobId = ingestionJobId;
    this.finishedDay = finishedDay;
    this.processedRows = processedRows;
    this.validRows = validRows;
  }
}
