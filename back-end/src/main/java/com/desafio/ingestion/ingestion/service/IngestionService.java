package com.desafio.ingestion.ingestion.service;

import com.desafio.ingestion.ingestion.application.port.out.CsvStorage;
import com.desafio.ingestion.ingestion.application.port.out.StoredCsv;
import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import com.desafio.ingestion.ingestion.validation.CsvHeaderValidator;
import com.desafio.ingestion.ingestion.validation.UploadPolicy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngestionService {
  private final IngestionJobRepository jobs;
  private final IngestionOutboxService outbox;
  private final CsvStorage storage;
  private final CsvHeaderValidator headerValidator;
  private final long maxUploadBytes;

  public IngestionService(
      IngestionJobRepository jobs,
      IngestionOutboxService outbox,
      CsvHeaderValidator headerValidator,
      CsvStorage storage,
      @Value("${app.upload.max-file-size:2GB}") DataSize maxUploadSize) {
    this.jobs = jobs;
    this.outbox = outbox;
    this.storage = storage;
    this.headerValidator = headerValidator;
    this.maxUploadBytes = maxUploadSize.toBytes();
  }

  @Transactional
  public IngestionJob accept(MultipartFile file) throws IOException {
    UploadPolicy.validateCsv(file.getOriginalFilename(), file.getSize(), maxUploadBytes);
    UUID id = UUID.randomUUID();
    Path storedPath = null;

    try {
      StoredCsv stored;
      try (InputStream input = file.getInputStream()) {
        stored = storage.store(id, input);
      }

      storedPath = stored.path();

      var inspection = headerValidator.inspect(stored.path());
      var dialect = inspection.dialect();

      IngestionJob job =
          jobs.save(
              new IngestionJob(
                  id,
                  file.getOriginalFilename(),
                  stored.path().toString(),
                  stored.sizeBytes(),
                  inspection.headers(),
                  dialect.delimiter(),
                  dialect.encoding()));

      job.queued();
      jobs.save(job);
      outbox.enqueue(id);

      return job;
    } catch (IOException | RuntimeException ex) {
      try {
        storage.delete(storedPath);
      } catch (IOException cleanup) {
        ex.addSuppressed(cleanup);
      }

      throw ex;
    }
  }

  @Transactional(readOnly = true)
  public java.util.List<IngestionJob> active(int requestedLimit) {
    int limit = Math.min(Math.max(requestedLimit, 1), 50);

    return jobs.findByStatusInOrderByCreatedAtAsc(
        java.util.List.of(JobStatus.RECEIVED, JobStatus.QUEUED, JobStatus.PROCESSING),
        org.springframework.data.domain.PageRequest.of(0, limit));
  }

  @Transactional(readOnly = true)
  public IngestionJob find(UUID id) {
    return jobs.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Ingestion job not found"));
  }
}
