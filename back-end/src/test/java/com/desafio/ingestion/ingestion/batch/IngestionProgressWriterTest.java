package com.desafio.ingestion.ingestion.batch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.desafio.ingestion.ingestion.domain.JobProgress;
import com.desafio.ingestion.ingestion.service.IngestionProgressService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

class IngestionProgressWriterTest {
  @Test
  void updatesCountersAfterDelegateWrite() throws Exception {
    ItemWriter<DynamicCsvRow> delegate = mock(TypedItemWriter.class);
    IngestionProgressService progress = mock(IngestionProgressService.class);
    IngestionProgressTracker tracker = new IngestionProgressTracker();
    UUID jobId = UUID.randomUUID();
    tracker.initialize(jobId, 0, 0, 0);
    tracker.beginChunk();
    tracker.recordRead();

    new IngestionProgressWriter(delegate, tracker, progress)
        .write(Chunk.of(new DynamicCsvRow(jobId, 1, "{}")));

    verify(delegate).write(Chunk.of(new DynamicCsvRow(jobId, 1, "{}")));
    verify(progress).update(jobId, new JobProgress(1, 1, 0));
  }

  private interface TypedItemWriter extends ItemWriter<DynamicCsvRow> {}
}
