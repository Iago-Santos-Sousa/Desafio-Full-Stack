package com.desafio.ingestion.ingestion.validation;

import java.util.Locale;

public final class UploadPolicy {
  private UploadPolicy() {}

  public static void validateCsv(String filename, long sizeBytes, long maxSizeBytes) {
    if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
      throw new IllegalArgumentException("CSV file is required");
    }

    if (sizeBytes > maxSizeBytes) {
      throw new UploadSizeExceededException(maxSizeBytes);
    }
  }
}
