package com.desafio.ingestion.analytics.service;

import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DateRangeResolver {
  private static final int MIN_SUPPORTED_YEAR = 1900;
  private final ZoneId zone;

  public DateRangeResolver(@Value("${app.business-time-zone:America/Sao_Paulo}") String zoneId) {
    this.zone = ZoneId.of(zoneId);
  }

  public DateRange resolve(LocalDate from, LocalDate to) {
    LocalDate today = LocalDate.now(zone);
    LocalDate resolvedFrom = from == null ? today.withDayOfMonth(1) : from;
    LocalDate resolvedTo = to == null ? today : to;

    LocalDate minimum = LocalDate.of(MIN_SUPPORTED_YEAR, 1, 1);

    if (resolvedFrom.isBefore(minimum)
        || resolvedTo.isBefore(minimum)
        || resolvedFrom.isAfter(today)
        || resolvedTo.isAfter(today)) {
      throw new InvalidDateRangeException("dates must be between " + minimum + " and " + today);
    }

    if (resolvedFrom.isAfter(resolvedTo)) {
      throw new InvalidDateRangeException("from must be before or equal to to");
    }

    return new DateRange(resolvedFrom, resolvedTo);
  }
}
