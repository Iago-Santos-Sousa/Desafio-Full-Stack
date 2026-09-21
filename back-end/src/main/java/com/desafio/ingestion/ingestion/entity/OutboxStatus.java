package com.desafio.ingestion.ingestion.entity;

public enum OutboxStatus {
  PENDING,
  RETRY,
  PUBLISHED,
  DEAD
}
