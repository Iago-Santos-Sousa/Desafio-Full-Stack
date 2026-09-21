package com.desafio.ingestion.ingestion.validation;

public class UploadSizeExceededException extends RuntimeException {
  public UploadSizeExceededException(long maxBytes) {
    super("CSV file exceeds maximum allowed size of " + maxBytes + " bytes");
  }
}
