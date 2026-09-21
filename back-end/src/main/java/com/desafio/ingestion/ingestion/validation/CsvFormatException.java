package com.desafio.ingestion.ingestion.validation;

public class CsvFormatException extends IllegalArgumentException {
  private final String code;

  public CsvFormatException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
