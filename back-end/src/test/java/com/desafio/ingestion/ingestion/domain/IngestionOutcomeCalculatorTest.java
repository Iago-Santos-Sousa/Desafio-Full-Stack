package com.desafio.ingestion.ingestion.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.desafio.ingestion.ingestion.entity.JobStatus;
import org.junit.jupiter.api.Test;

class IngestionOutcomeCalculatorTest {
  @Test
  void mapsSuccessfulBatchWithSkippedRowsToCompletedWithErrors() {
    JobOutcome outcome = IngestionOutcomeCalculator.calculate(true, 8, 2);

    assertThat(outcome.status()).isEqualTo(JobStatus.COMPLETED_WITH_ERRORS);
    assertThat(outcome.progress()).isEqualTo(new JobProgress(10, 8, 2));
    assertThat(outcome.errorSummary()).isNull();
  }

  @Test
  void mapsFailedBatchToTerminalFailure() {
    JobOutcome outcome = IngestionOutcomeCalculator.calculate(false, 8, 2);

    assertThat(outcome.status()).isEqualTo(JobStatus.FAILED);
    assertThat(outcome.errorSummary()).isEqualTo("Batch processing failed");
  }

  @Test
  void rejectsCountersThatDoNotBalance() {
    assertThatThrownBy(() -> new JobProgress(10, 9, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("valid plus invalid");
  }
}
