package com.desafio.ingestion.ingestion.validation;

import java.util.List;

public record CsvInspection(List<String> headers, CsvDialect dialect) {
  public CsvInspection {
    headers = List.copyOf(headers);
  }
}
