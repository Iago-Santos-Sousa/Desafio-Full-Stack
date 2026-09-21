package com.desafio.ingestion.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.desafio.ingestion.analytics.dto.AggregateDto;
import com.desafio.ingestion.analytics.dto.SummaryDto;
import com.desafio.ingestion.analytics.repository.AnalyticsRepository;
import com.desafio.ingestion.analytics.repository.MonthlyMetricProjection;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class AnalyticsServiceTest {
  private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
  private static final LocalDate TO = LocalDate.of(2026, 1, 31);

  @Test
  void resolvesRangeBeforeReadingSummary() {
    AnalyticsRepository repository = mock(AnalyticsRepository.class);
    DateRangeResolver resolver = mock(DateRangeResolver.class);
    DateRange range = new DateRange(FROM, TO);
    SummaryDto expected = new SummaryDto(10, 8, 2);
    when(resolver.resolve(FROM, TO)).thenReturn(range);
    when(repository.findSummary(FROM, TO)).thenReturn(expected);

    SummaryDto actual =
        new AnalyticsService(repository, resolver, "America/Sao_Paulo").summary(FROM, TO);

    assertThat(actual).isEqualTo(expected);
    verify(resolver).resolve(FROM, TO);
    verify(repository).findSummary(FROM, TO);
  }

  @Test
  void mapsMonthlyProjectionsToPublicAggregates() {
    AnalyticsRepository repository = mock(AnalyticsRepository.class);
    DateRangeResolver resolver = mock(DateRangeResolver.class);
    MonthlyMetricProjection projection = mock(MonthlyMetricProjection.class);
    when(resolver.resolve(FROM, TO)).thenReturn(new DateRange(FROM, TO));
    when(repository.findMonthlyMetrics(FROM, TO)).thenReturn(List.of(projection));
    when(projection.getMonth()).thenReturn(LocalDate.of(2026, 1, 1));
    when(projection.getRecordCount()).thenReturn(10L);

    List<AggregateDto> result =
        new AnalyticsService(repository, resolver, "America/Sao_Paulo").monthlyAggregates(FROM, TO);

    assertThat(result).containsExactly(new AggregateDto(LocalDate.of(2026, 1, 1), 10));
  }

  @Test
  void refreshesMetricIdempotentlyByDeletingBeforeInserting() {
    AnalyticsRepository repository = mock(AnalyticsRepository.class);
    DateRangeResolver resolver = mock(DateRangeResolver.class);
    UUID jobId = UUID.randomUUID();
    Instant finishedAt = Instant.parse("2026-01-15T03:00:00Z");

    new AnalyticsService(repository, resolver, "America/Sao_Paulo")
        .refreshMetric(jobId, 20, 18, finishedAt);

    InOrder order = inOrder(repository);
    order.verify(repository).deleteByJobId(jobId);
    order.verify(repository).insertMetric(jobId, LocalDate.of(2026, 1, 15), 20, 18);
  }
}
