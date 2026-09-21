package com.desafio.ingestion.analytics.service;

import com.desafio.ingestion.analytics.dto.AggregateDto;
import com.desafio.ingestion.analytics.dto.SummaryDto;
import com.desafio.ingestion.analytics.repository.AnalyticsRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
  private final AnalyticsRepository repository;
  private final DateRangeResolver dateRangeResolver;
  private final ZoneId businessZone;

  public AnalyticsService(
      AnalyticsRepository repository,
      DateRangeResolver dateRangeResolver,
      @Value("${app.business-time-zone:America/Sao_Paulo}") String businessTimeZone) {
    this.repository = repository;
    this.dateRangeResolver = dateRangeResolver;
    this.businessZone = ZoneId.of(businessTimeZone);
  }

  @Transactional(readOnly = true)
  public SummaryDto summary(LocalDate from, LocalDate to) {
    DateRange range = dateRangeResolver.resolve(from, to);
    return repository.findSummary(range.from(), range.to());
  }

  @Transactional(readOnly = true)
  public List<AggregateDto> monthlyAggregates(LocalDate from, LocalDate to) {
    DateRange range = dateRangeResolver.resolve(from, to);

    return repository.findMonthlyMetrics(range.from(), range.to()).stream()
        .map(row -> new AggregateDto(row.getMonth(), row.getRecordCount()))
        .toList();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void refreshMetric(UUID jobId, long processedRows, long validRows, Instant finishedAt) {
    repository.deleteByJobId(jobId);

    repository.insertMetric(
        jobId, finishedAt.atZone(businessZone).toLocalDate(), processedRows, validRows);
  }
}
