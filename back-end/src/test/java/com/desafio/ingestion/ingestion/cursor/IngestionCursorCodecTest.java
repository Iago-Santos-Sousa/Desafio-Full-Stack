package com.desafio.ingestion.ingestion.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.desafio.ingestion.ingestion.dto.IngestionJobListItem;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IngestionCursorCodecTest {
  @Test
  void encodesAndDecodesJobCursor() {
    UUID jobId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-01-02T03:04:05Z");
    IngestionJobListItem item =
        new IngestionJobListItem(jobId, "data.csv", 42, JobStatus.COMPLETED, createdAt, createdAt);

    IngestionCursorCodec.Cursor cursor =
        IngestionCursorCodec.decode(IngestionCursorCodec.encode(item));

    assertThat(cursor.createdAt()).isEqualTo(createdAt);
    assertThat(cursor.jobId()).isEqualTo(jobId);
  }

  @Test
  void returnsNullForMissingCursor() {
    assertThat(IngestionCursorCodec.decode(null)).isNull();
    assertThat(IngestionCursorCodec.decode(" ")).isNull();
  }

  @Test
  void rejectsMalformedCursor() {
    assertThatThrownBy(() -> IngestionCursorCodec.decode("not-a-cursor"))
        .isInstanceOf(InvalidCursorException.class);
  }
}
