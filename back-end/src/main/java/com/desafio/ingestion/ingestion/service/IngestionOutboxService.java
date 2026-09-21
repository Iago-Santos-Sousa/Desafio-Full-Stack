package com.desafio.ingestion.ingestion.service;

import com.desafio.ingestion.ingestion.entity.IngestionEventOutbox;
import com.desafio.ingestion.ingestion.repository.IngestionEventOutboxRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionOutboxService {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestionOutboxService.class);
  private final IngestionEventOutboxRepository outbox;

  public IngestionOutboxService(IngestionEventOutboxRepository outbox) {
    this.outbox = outbox;
  }

  @Transactional
  public void enqueue(UUID jobId) {
    outbox.save(new IngestionEventOutbox(jobId, Instant.now()));
    LOGGER.info("event=outbox_created jobId={}", jobId);
  }
}
