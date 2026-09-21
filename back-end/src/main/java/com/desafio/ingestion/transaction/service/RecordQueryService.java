package com.desafio.ingestion.transaction.service;

import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import com.desafio.ingestion.transaction.dto.RecordDto;
import com.desafio.ingestion.transaction.dto.RecordPageResponse;
import com.desafio.ingestion.transaction.repository.RecordRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecordQueryService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RecordQueryService.class);

  private final RecordRepository records;
  private final IngestionJobRepository jobs;
  private final long slowQueryThresholdMs;

  public RecordQueryService(
      RecordRepository records,
      IngestionJobRepository jobs,
      @Value("${app.records.slow-query-ms:1000}") long slowQueryThresholdMs) {
    this.records = records;
    this.jobs = jobs;
    this.slowQueryThresholdMs = slowQueryThresholdMs;
  }

  @Transactional(readOnly = true)
  public RecordPageResponse list(UUID jobId, int requestedSize, Long cursor) {
    long startedAt = System.nanoTime();
    var job = jobs.findById(jobId).orElseThrow(() -> new NoSuchElementException("Job not found"));
    int limit = Math.min(Math.max(requestedSize, 1), 200);
    var rows = records.findPage(limit, cursor, jobId);

    boolean hasMore = rows.size() > limit;

    if (hasMore) {
      rows = rows.subList(0, limit);
    }

    List<RecordDto> items =
        rows.stream().map(row -> new RecordDto(row.id(), row.rowNumber(), row.values())).toList();

    Long nextCursor = hasMore && !rows.isEmpty() ? rows.get(rows.size() - 1).id() : null;
    long totalRecords = job.getValidRows();
    long totalPages =
        totalRecords == 0 ? 0 : (totalRecords / limit) + (totalRecords % limit == 0 ? 0 : 1);

    long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

    if (durationMs >= slowQueryThresholdMs) {
      LOGGER.warn(
          "event=records_query jobId={} cursor={} size={} rowCount={} durationMs={}",
          jobId,
          cursor,
          limit,
          items.size(),
          durationMs);
    } else {
      LOGGER.debug(
          "event=records_query jobId={} cursor={} size={} rowCount={} durationMs={}",
          jobId,
          cursor,
          limit,
          items.size(),
          durationMs);
    }

    return new RecordPageResponse(job.getColumns(), items, nextCursor, totalRecords, totalPages);
  }
}
