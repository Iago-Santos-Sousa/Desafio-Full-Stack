package com.desafio.ingestion.ingestion.batch;

import com.desafio.ingestion.ingestion.domain.JobProgress;
import java.util.UUID;

public class IngestionProgressTracker {
  private UUID jobId;
  private long processedRows;
  private long validRows;
  private long invalidRows;
  private long chunkProcessedRows;
  private long chunkValidRows;
  private long chunkInvalidRows;
  private boolean checkpointed;

  public void initialize(UUID jobId, long processedRows, long validRows, long invalidRows) {
    this.jobId = jobId;
    this.processedRows = processedRows;
    this.validRows = validRows;
    this.invalidRows = invalidRows;
  }

  public void beginChunk() {
    chunkProcessedRows = processedRows;
    chunkValidRows = validRows;
    chunkInvalidRows = invalidRows;
    checkpointed = false;
  }

  public void recordRead() {
    processedRows++;
  }

  public void recordValid(long count) {
    validRows += count;
  }

  public void recordInvalid() {
    invalidRows++;
  }

  public void markCheckpointed() {
    checkpointed = true;
  }

  public boolean hasChanges() {
    return processedRows != chunkProcessedRows
        || validRows != chunkValidRows
        || invalidRows != chunkInvalidRows;
  }

  public boolean isCheckpointed() {
    return checkpointed;
  }

  public void rollbackChunk() {
    processedRows = chunkProcessedRows;
    validRows = chunkValidRows;
    invalidRows = chunkInvalidRows;
    checkpointed = false;
  }

  public JobProgress snapshot() {
    return new JobProgress(processedRows, validRows, invalidRows);
  }

  public UUID jobId() {
    return jobId;
  }
}
