package com.desafio.ingestion.ingestion.domain;

public record JobProgress(long processedRows, long validRows, long invalidRows) {
  public JobProgress {
    if (processedRows < 0 || validRows < 0 || invalidRows < 0) {
      throw new IllegalArgumentException("Job counters cannot be negative");
    }

    if (processedRows != validRows + invalidRows) {
      throw new IllegalArgumentException("Processed rows must equal valid plus invalid rows");
    }
  }
}
