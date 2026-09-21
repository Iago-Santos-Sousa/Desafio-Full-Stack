package com.desafio.ingestion.ingestion.validation;

import java.nio.charset.Charset;
import java.util.Objects;

public record CsvDialect(char delimiter, Charset charset) {
  public CsvDialect {
    Objects.requireNonNull(charset, "charset");

    if (delimiter == '\n' || delimiter == '\r' || delimiter == '"') {
      throw new IllegalArgumentException("CSV delimiter is not supported");
    }
  }

  public String encoding() {
    return charset.name();
  }
}
