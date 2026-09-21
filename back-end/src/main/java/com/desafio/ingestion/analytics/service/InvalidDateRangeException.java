package com.desafio.ingestion.analytics.service;

public class InvalidDateRangeException extends IllegalArgumentException {
  public InvalidDateRangeException(String message) {
    super(message);
  }
}
