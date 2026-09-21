package com.desafio.ingestion.ingestion.cursor;

public class InvalidCursorException extends IllegalArgumentException {
  public InvalidCursorException(String message, Throwable cause) {
    super(message, cause);
  }
}
