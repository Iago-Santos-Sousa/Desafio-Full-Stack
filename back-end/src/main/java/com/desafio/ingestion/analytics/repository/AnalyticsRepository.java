package com.desafio.ingestion.analytics.repository;

import com.desafio.ingestion.analytics.dto.SummaryDto;
import com.desafio.ingestion.analytics.entity.IngestionJobMetric;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AnalyticsRepository extends Repository<IngestionJobMetric, UUID> {
  @Query(
      "select new com.desafio.ingestion.analytics.dto.SummaryDto("
          + "coalesce(sum(m.processedRows), 0), "
          + "coalesce(sum(m.validRows), 0), "
          + "count(m.ingestionJobId)) "
          + "from IngestionJobMetric m "
          + "where m.finishedDay between :from and :to")
  SummaryDto findSummary(@Param("from") LocalDate from, @Param("to") LocalDate to);

  @Query(
      value =
          "select date_trunc('month', finished_day)::date as month, "
              + "sum(processed_rows) as recordCount "
              + "from ingestion_job_metric "
              + "where finished_day between :from and :to "
              + "group by 1 order by 1",
      nativeQuery = true)
  java.util.List<MonthlyMetricProjection> findMonthlyMetrics(
      @Param("from") LocalDate from, @Param("to") LocalDate to);

  @Modifying
  @Query("delete from IngestionJobMetric m where m.ingestionJobId = :jobId")
  int deleteByJobId(@Param("jobId") UUID jobId);

  @Modifying
  @Query(
      value =
          "insert into ingestion_job_metric "
              + "(ingestion_job_id, finished_day, processed_rows, valid_rows) "
              + "values (:jobId, :finishedDay, :processedRows, :validRows)",
      nativeQuery = true)
  int insertMetric(
      @Param("jobId") UUID jobId,
      @Param("finishedDay") LocalDate finishedDay,
      @Param("processedRows") long processedRows,
      @Param("validRows") long validRows);
}
