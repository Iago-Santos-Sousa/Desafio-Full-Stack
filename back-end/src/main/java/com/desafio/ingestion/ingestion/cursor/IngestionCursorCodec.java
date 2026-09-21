package com.desafio.ingestion.ingestion.cursor;

import com.desafio.ingestion.ingestion.dto.IngestionJobListItem;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public final class IngestionCursorCodec {
  private IngestionCursorCodec() {}

  public static Cursor decode(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      String decoded = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
      String[] parts = decoded.split("\\|", -1);

      if (parts.length != 2) {
        throw new InvalidCursorException("Invalid ingestion cursor", null);
      }

      return new Cursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));

    } catch (RuntimeException exception) {
      if (exception instanceof InvalidCursorException invalid) {
        throw invalid;
      }

      throw new InvalidCursorException("Invalid ingestion cursor", exception);
    }
  }

  public static String encode(IngestionJobListItem item) {
    String value = item.createdAt() + "|" + item.jobId();
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  public record Cursor(Instant createdAt, UUID jobId) {}
}
