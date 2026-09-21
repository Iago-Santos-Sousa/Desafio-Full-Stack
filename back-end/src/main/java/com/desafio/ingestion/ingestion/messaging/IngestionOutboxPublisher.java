package com.desafio.ingestion.ingestion.messaging;

import static com.desafio.ingestion.shared.MessagingConfig.INGESTION_ROUTING_KEY;

import com.desafio.ingestion.ingestion.entity.IngestionEventOutbox;
import com.desafio.ingestion.ingestion.entity.OutboxStatus;
import com.desafio.ingestion.ingestion.repository.IngestionEventOutboxRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IngestionOutboxPublisher {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestionOutboxPublisher.class);
  private static final List<OutboxStatus> PUBLISHABLE =
      List.of(OutboxStatus.PENDING, OutboxStatus.RETRY);

  private final IngestionEventOutboxRepository outbox;
  private final RabbitTemplate rabbit;

  public IngestionOutboxPublisher(IngestionEventOutboxRepository outbox, RabbitTemplate rabbit) {
    this.outbox = outbox;
    this.rabbit = rabbit;
  }

  @Scheduled(fixedDelayString = "${app.messaging.outbox-interval-ms:1000}")
  @Transactional
  public void publishPending() {
    Instant now = Instant.now();

    List<IngestionEventOutbox> events =
        outbox.findTop20ByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            PUBLISHABLE, now);

    for (IngestionEventOutbox event : events) {
      publish(event, now);
    }
  }

  private void publish(IngestionEventOutbox event, Instant now) {
    try {
      CorrelationData correlation = new CorrelationData(event.getId().toString());

      rabbit.convertAndSend(
          "ingestion.exchange",
          INGESTION_ROUTING_KEY,
          new JobMessage(event.getJobId()),
          correlation);

      CorrelationData.Confirm confirm = correlation.getFuture().get(10, TimeUnit.SECONDS);

      if (!confirm.ack() || correlation.getReturned() != null) {
        LOGGER.warn(
            "event=publish_rejected jobId={} outboxId={} reason={}",
            event.getJobId(),
            event.getId(),
            confirm.reason());

        throw new IllegalStateException(
            confirm.reason() == null ? "RabbitMQ rejected message" : confirm.reason());
      }

      event.markPublished(now);

      LOGGER.info(
          "event=publish_confirmed jobId={} outboxId={} publishedAt={}",
          event.getJobId(),
          event.getId(),
          event.getPublishedAt());

    } catch (Exception exception) {
      Duration delay = Duration.ofSeconds(Math.min(10, 1L << event.getAttempts()));
      event.markRetry(exception.getMessage(), now.plus(delay));

      String retryEvent =
          event.getStatus() == OutboxStatus.DEAD ? "job_dead_lettered" : "job_retry";

      LOGGER.error(
          "event=publish_failed jobId={} outboxId={} attempt={} status={}",
          event.getJobId(),
          event.getId(),
          event.getAttempts(),
          event.getStatus(),
          exception);

      LOGGER.warn(
          "event={} jobId={} outboxId={} attempt={}",
          retryEvent,
          event.getJobId(),
          event.getId(),
          event.getAttempts());
    }
  }
}
