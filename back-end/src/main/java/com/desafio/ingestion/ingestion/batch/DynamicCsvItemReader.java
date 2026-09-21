package com.desafio.ingestion.ingestion.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;

public class DynamicCsvItemReader implements ItemStreamReader<DynamicCsvRow> {
  private static final int MAX_VALUE_LENGTH = 1_000_000;

  private final Path file;
  private final UUID jobId;
  private final ObjectMapper objectMapper;
  private final char delimiter;
  private final Charset charset;
  private final IngestionProgressTracker progress;
  private CSVParser parser;
  private Iterator<CSVRecord> records;
  private List<String> headers;
  private long rowNumber;

  public DynamicCsvItemReader(Path file, UUID jobId, ObjectMapper objectMapper) {
    this(file, jobId, objectMapper, ',', StandardCharsets.UTF_8, null);
  }

  public DynamicCsvItemReader(
      Path file, UUID jobId, ObjectMapper objectMapper, char delimiter, Charset charset) {
    this(file, jobId, objectMapper, delimiter, charset, null);
  }

  public DynamicCsvItemReader(
      Path file,
      UUID jobId,
      ObjectMapper objectMapper,
      char delimiter,
      Charset charset,
      IngestionProgressTracker progress) {
    this.file = file;
    this.jobId = jobId;
    this.objectMapper = objectMapper;
    this.delimiter = delimiter;
    this.charset = charset;
    this.progress = progress;
  }

  @Override
  public void open(ExecutionContext executionContext) {
    try {
      var decoder = charset.newDecoder();
      decoder.onMalformedInput(CodingErrorAction.REPORT);
      decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
      Reader reader = new InputStreamReader(Files.newInputStream(file), decoder);
      parser = CSVFormat.RFC4180.builder().setDelimiter(delimiter).get().parse(reader);

      Iterator<CSVRecord> allRecords = parser.iterator();

      if (!allRecords.hasNext()) {
        throw new IllegalArgumentException("CSV must contain a header");
      }

      CSVRecord headerRecord = allRecords.next();
      headers = headerRecord.stream().map(this::stripBom).map(String::trim).toList();
      records = allRecords;
      rowNumber = executionContext.getLong("csv.rowNumber", 0L);
    } catch (java.io.IOException exception) {
      throw new IllegalStateException("Could not open CSV file", exception);
    }
  }

  @Override
  public DynamicCsvRow read() {
    if (records == null || !records.hasNext()) {
      return null;
    }

    CSVRecord record = records.next();

    rowNumber++;

    if (progress != null) {
      progress.recordRead();
    }

    if (record.size() != headers.size()) {
      throw new IllegalArgumentException("CSV row has a different number of columns");
    }

    var values = new LinkedHashMap<String, String>();

    for (int index = 0; index < headers.size(); index++) {
      String value = record.get(index);

      if (value.length() > MAX_VALUE_LENGTH) {
        throw new IllegalArgumentException("CSV field exceeds the maximum size");
      }

      values.put(headers.get(index), value);
    }

    try {
      return new DynamicCsvRow(jobId, rowNumber, objectMapper.writeValueAsString(values));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Could not serialize CSV row", exception);
    }
  }

  @Override
  public void update(ExecutionContext executionContext) {
    executionContext.putLong("csv.rowNumber", rowNumber);
  }

  @Override
  public void close() {
    if (parser != null) {
      try {
        parser.close();
      } catch (java.io.IOException exception) {
        throw new IllegalStateException("Could not close CSV file", exception);
      }
    }
  }

  private String stripBom(String value) {
    return value.startsWith("\uFEFF") ? value.substring(1) : value;
  }
}
