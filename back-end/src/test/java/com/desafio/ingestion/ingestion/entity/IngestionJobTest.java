package com.desafio.ingestion.ingestion.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.desafio.ingestion.ingestion.domain.InvalidJobTransitionException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IngestionJobTest {
  @Test
  void followsQueueProcessingAndSuccessfulCompletionTransitions() {
    IngestionJob job =
        new IngestionJob(UUID.randomUUID(), "data.csv", "/tmp/data.csv", 10, List.of("name"));

    job.queued();
    assertThat(job.getStatus()).isEqualTo(JobStatus.QUEUED);
    assertThat(job.getQueuedAt()).isNotNull();

    job.processing();
    assertThat(job.getStatus()).isEqualTo(JobStatus.PROCESSING);
    assertThat(job.getStartedAt()).isNotNull();

    job.finish(JobStatus.COMPLETED_WITH_ERRORS, 8, 7, 1, null);
    assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED_WITH_ERRORS);
    assertThat(job.getTotalRows()).isEqualTo(8);
    assertThat(job.getProcessedRows()).isEqualTo(8);
    assertThat(job.getValidRows()).isEqualTo(7);
    assertThat(job.getInvalidRows()).isEqualTo(1);
    assertThat(job.getFinishedAt()).isNotNull();
  }

  @Test
  void assignsFallbackErrorWhenFailureReasonIsMissing() {
    IngestionJob job =
        new IngestionJob(UUID.randomUUID(), "data.csv", "/tmp/data.csv", 10, List.of("name"));

    job.fail(null);

    assertThat(job.getStatus()).isEqualTo(JobStatus.FAILED);
    assertThat(job.getErrorSummary()).isEqualTo("Ingestion processing failed");
    assertThat(job.getFinishedAt()).isNotNull();
  }

  @Test
  void doesNotAllowMutationAfterTerminalState() {
    IngestionJob job =
        new IngestionJob(UUID.randomUUID(), "data.csv", "/tmp/data.csv", 10, List.of("name"));
    job.finish(JobStatus.COMPLETED, 0, 0, 0, null);

    assertThatThrownBy(job::processing).isInstanceOf(InvalidJobTransitionException.class);
  }
}
