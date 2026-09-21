package com.desafio.ingestion.analytics.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateRangeResolverTest {
  private final DateRangeResolver resolver = new DateRangeResolver("America/Sao_Paulo");

  @Test
  void acceptsLeapDayWithinSupportedRange() {
    assertDoesNotThrow(
        () -> resolver.resolve(LocalDate.of(2024, 2, 29), LocalDate.of(2024, 2, 29)));
  }

  @Test
  void rejectsYearBeforeSupportedRange() {
    assertThrows(
        InvalidDateRangeException.class,
        () -> resolver.resolve(LocalDate.of(236, 2, 29), LocalDate.of(236, 2, 29)));
  }

  @Test
  void rejectsFutureDate() {
    LocalDate tomorrow = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).plusDays(1);

    assertThrows(InvalidDateRangeException.class, () -> resolver.resolve(tomorrow, tomorrow));
  }

  @Test
  void rejectsReversedRange() {
    assertThrows(
        InvalidDateRangeException.class,
        () -> resolver.resolve(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 31)));
  }
}
