package com.desafio.ingestion.ingestion.batch;

import com.desafio.ingestion.ingestion.service.IngestionProgressService;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

public class IngestionProgressWriter implements ItemWriter<DynamicCsvRow> {
  private final ItemWriter<DynamicCsvRow> delegate;
  private final IngestionProgressTracker tracker;
  private final IngestionProgressService progress;

  public IngestionProgressWriter(
      ItemWriter<DynamicCsvRow> delegate,
      IngestionProgressTracker tracker,
      IngestionProgressService progress) {
    this.delegate = delegate;
    this.tracker = tracker;
    this.progress = progress;
  }

  @Override
  public void write(Chunk<? extends DynamicCsvRow> items) throws Exception {
    delegate.write(items);
    tracker.recordValid(items.size());
    progress.update(tracker.jobId(), tracker.snapshot());
    tracker.markCheckpointed();
  }
}
