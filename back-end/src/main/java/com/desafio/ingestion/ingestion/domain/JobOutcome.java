package com.desafio.ingestion.ingestion.domain;

import com.desafio.ingestion.ingestion.entity.JobStatus;
import java.util.Objects;

public record JobOutcome(JobStatus status, JobProgress progress, String errorSummary) {
  public JobOutcome {
    Objects.requireNonNull(progress, "Job progress is required");

    if (status != JobStatus.COMPLETED
        && status != JobStatus.COMPLETED_WITH_ERRORS
        && status != JobStatus.FAILED) {
      throw new IllegalArgumentException("Job outcome must be terminal");
    }

    if (status == JobStatus.FAILED && (errorSummary == null || errorSummary.isBlank())) {
      throw new IllegalArgumentException("Failed job must have an error summary");
    }

    if (status == JobStatus.COMPLETED && progress.invalidRows() > 0) {
      throw new IllegalArgumentException("Completed job cannot contain invalid rows");
    }
  }
}
