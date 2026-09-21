package com.desafio.ingestion.ingestion.domain;

public class InvalidJobTransitionException extends IllegalStateException {
  public InvalidJobTransitionException(String message) {
    super(message);
  }
}
