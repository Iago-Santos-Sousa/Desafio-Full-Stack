package com.desafio.ingestion.ingestion.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.item.ExecutionContext;

class DynamicCsvItemReaderTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final UUID jobId = UUID.randomUUID();

  @Test
  void readsDynamicColumnsIncrementally(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("dynamic.csv");
    Files.writeString(file, "name,amount\nAda,10\nBob,20\n");
    DynamicCsvItemReader reader = new DynamicCsvItemReader(file, jobId, objectMapper);

    reader.open(new ExecutionContext());

    DynamicCsvRow first = reader.read();
    DynamicCsvRow second = reader.read();

    assertThat(first.jobId()).isEqualTo(jobId);
    assertThat(first.rowNumber()).isEqualTo(1);
    assertThat(first.dataJson()).contains("\"name\":\"Ada\"");
    assertThat(second.rowNumber()).isEqualTo(2);
    assertThat(reader.read()).isNull();
    reader.close();
  }

  @Test
  void resumesRowNumberFromExecutionContext(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("resume.csv");
    Files.writeString(file, "name\nAda\n");
    DynamicCsvItemReader reader = new DynamicCsvItemReader(file, jobId, objectMapper);
    ExecutionContext context = new ExecutionContext();
    context.putLong("csv.rowNumber", 4);

    reader.open(context);

    assertThat(reader.read().rowNumber()).isEqualTo(5);
    reader.close();
  }

  @Test
  void rejectsRowsWithDifferentColumnCount(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("invalid-row.csv");
    Files.writeString(file, "name,amount\nAda\n");
    DynamicCsvItemReader reader = new DynamicCsvItemReader(file, jobId, objectMapper);
    reader.open(new ExecutionContext());

    assertThatThrownBy(reader::read)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("different number");
    reader.close();
  }

  @Test
  void readsConfiguredSemicolonDialect(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("semicolon.csv");
    Files.writeString(file, "name;amount\nAda;10\n");

    DynamicCsvItemReader reader =
        new DynamicCsvItemReader(file, jobId, objectMapper, ';', StandardCharsets.UTF_8);

    reader.open(new ExecutionContext());

    assertThat(reader.read().dataJson()).contains("\"name\":\"Ada\"");
    reader.close();
  }
}
