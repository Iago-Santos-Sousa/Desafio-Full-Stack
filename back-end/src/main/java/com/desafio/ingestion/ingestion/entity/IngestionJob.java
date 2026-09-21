package com.desafio.ingestion.ingestion.entity;

import com.desafio.ingestion.ingestion.domain.InvalidJobTransitionException;
import com.desafio.ingestion.ingestion.domain.JobProgress;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ingestion_job")
public class IngestionJob {
  @Id private UUID id;

  @Column(nullable = false)
  private String originalFilename;

  @Column(nullable = false)
  private String storedPath;

  private long fileSizeBytes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private JobStatus status;

  private long totalRows;
  private long processedRows;
  private long validRows;
  private long invalidRows;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private List<String> columns = new ArrayList<>();

  @Column(nullable = false, length = 1)
  private String csvDelimiter = ",";

  @Column(nullable = false, length = 40)
  private String csvEncoding = "UTF-8";

  @Column(length = 1000)
  private String errorSummary;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant queuedAt;
  private Instant startedAt;
  private Instant finishedAt;
  private Instant updatedAt;

  protected IngestionJob() {}

  public IngestionJob(
      UUID id,
      String originalFilename,
      String storedPath,
      long fileSizeBytes,
      List<String> columns) {
    this(id, originalFilename, storedPath, fileSizeBytes, columns, ',', "UTF-8");
  }

  public IngestionJob(
      UUID id,
      String originalFilename,
      String storedPath,
      long fileSizeBytes,
      List<String> columns,
      char csvDelimiter,
      String csvEncoding) {
    this.id = id;
    this.originalFilename = originalFilename;
    this.storedPath = storedPath;
    this.fileSizeBytes = fileSizeBytes;
    this.columns = List.copyOf(columns);
    this.csvDelimiter = String.valueOf(csvDelimiter);
    this.csvEncoding = csvEncoding;
    this.status = JobStatus.RECEIVED;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getOriginalFilename() {
    return originalFilename;
  }

  public String getStoredPath() {
    return storedPath;
  }

  public long getFileSizeBytes() {
    return fileSizeBytes;
  }

  public JobStatus getStatus() {
    return status;
  }

  public long getTotalRows() {
    return totalRows;
  }

  public long getProcessedRows() {
    return processedRows;
  }

  public long getValidRows() {
    return validRows;
  }

  public long getInvalidRows() {
    return invalidRows;
  }

  public List<String> getColumns() {
    return columns;
  }

  public String getCsvDelimiter() {
    return csvDelimiter;
  }

  public String getCsvEncoding() {
    return csvEncoding;
  }

  public String getErrorSummary() {
    return errorSummary;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getQueuedAt() {
    return queuedAt;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void queued() {
    ensureMutable("queue");
    status = JobStatus.QUEUED;
    Instant now = Instant.now();
    queuedAt = now;
    updatedAt = now;
  }

  public void processing() {
    ensureMutable("start processing");
    status = JobStatus.PROCESSING;
    Instant now = Instant.now();
    startedAt = now;
    updatedAt = now;
  }

  public void finish(
      JobStatus finalStatus, long processed, long valid, long invalid, String error) {
    ensureMutable("finish");

    JobProgress progress = new JobProgress(processed, valid, invalid);

    if (finalStatus != JobStatus.COMPLETED
        && finalStatus != JobStatus.COMPLETED_WITH_ERRORS
        && finalStatus != JobStatus.FAILED) {
      throw new IllegalArgumentException("Final job status must be terminal");
    }

    if (finalStatus == JobStatus.FAILED && (error == null || error.isBlank())) {
      throw new IllegalArgumentException("Failed job must have an error summary");
    }

    if (finalStatus == JobStatus.COMPLETED && progress.invalidRows() > 0) {
      throw new IllegalArgumentException("Completed job cannot contain invalid rows");
    }

    status = finalStatus;
    totalRows = progress.processedRows();
    processedRows = progress.processedRows();
    validRows = progress.validRows();
    invalidRows = progress.invalidRows();
    errorSummary = error;
    Instant now = Instant.now();
    finishedAt = now;
    updatedAt = now;
  }

  public void updateProgress(JobProgress progress) {
    ensureMutable("update progress");
    totalRows = progress.processedRows();
    processedRows = progress.processedRows();
    validRows = progress.validRows();
    invalidRows = progress.invalidRows();
    updatedAt = Instant.now();
  }

  public void fail(String error) {
    ensureMutable("fail");
    status = JobStatus.FAILED;
    errorSummary = error == null ? "Ingestion processing failed" : error;
    Instant now = Instant.now();
    finishedAt = now;
    updatedAt = now;
  }

  private void ensureMutable(String operation) {
    if (status == JobStatus.COMPLETED
        || status == JobStatus.COMPLETED_WITH_ERRORS
        || status == JobStatus.FAILED) {
      throw new InvalidJobTransitionException("Cannot " + operation + " terminal job " + id);
    }
  }
}
