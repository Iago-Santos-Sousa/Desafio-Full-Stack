package com.desafio.ingestion.ingestion.repository;

import com.desafio.ingestion.ingestion.domain.JobProgress;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BatchProgressRepository {
  private static final String LATEST_PROGRESS_QUERY =
      "SELECT (se.READ_COUNT + se.READ_SKIP_COUNT) AS processed_rows, "
          + "se.WRITE_COUNT AS valid_rows, "
          + "(se.READ_SKIP_COUNT + se.PROCESS_SKIP_COUNT + se.WRITE_SKIP_COUNT) "
          + "AS invalid_rows "
          + "FROM BATCH_STEP_EXECUTION se "
          + "JOIN BATCH_JOB_EXECUTION je ON je.JOB_EXECUTION_ID = se.JOB_EXECUTION_ID "
          + "JOIN BATCH_JOB_EXECUTION_PARAMS p ON p.JOB_EXECUTION_ID = je.JOB_EXECUTION_ID "
          + "WHERE p.PARAMETER_NAME = 'jobId' AND p.PARAMETER_VALUE = ? "
          + "ORDER BY se.LAST_UPDATED DESC NULLS LAST, se.STEP_EXECUTION_ID DESC LIMIT 1";

  private final JdbcTemplate jdbc;

  public BatchProgressRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<JobProgress> findLatest(UUID jobId) {
    JobProgress progress =
        jdbc.query(
            LATEST_PROGRESS_QUERY,
            resultSet -> {
              if (!resultSet.next()) {
                return null;
              }

              return new JobProgress(
                  resultSet.getLong("processed_rows"),
                  resultSet.getLong("valid_rows"),
                  resultSet.getLong("invalid_rows"));
            },
            jobId.toString());

    return Optional.ofNullable(progress);
  }
}
