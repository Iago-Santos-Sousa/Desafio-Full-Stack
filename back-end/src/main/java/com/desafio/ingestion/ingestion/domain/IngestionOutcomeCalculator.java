package com.desafio.ingestion.ingestion.domain;

import com.desafio.ingestion.ingestion.entity.JobStatus;

public final class IngestionOutcomeCalculator {
  public static final String BATCH_FAILURE_MESSAGE = "Batch processing failed";

  private IngestionOutcomeCalculator() {}

  public static JobOutcome calculate(boolean batchSucceeded, long validRows, long invalidRows) {
    JobProgress progress = new JobProgress(validRows + invalidRows, validRows, invalidRows);

    if (!batchSucceeded) {
      return new JobOutcome(JobStatus.FAILED, progress, BATCH_FAILURE_MESSAGE);
    }

    JobStatus status = invalidRows > 0 ? JobStatus.COMPLETED_WITH_ERRORS : JobStatus.COMPLETED;

    return new JobOutcome(status, progress, null);
  }
}
