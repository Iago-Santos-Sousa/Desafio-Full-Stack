package com.desafio.ingestion.analytics.repository;

import java.time.LocalDate;

public interface MonthlyMetricProjection {
  LocalDate getMonth();

  long getRecordCount();
}
