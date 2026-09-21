package com.desafio.ingestion.ingestion.messaging;

import static com.desafio.ingestion.shared.MessagingConfig.INGESTION_QUEUE;

import com.desafio.ingestion.ingestion.service.IngestionProcessingService;
import com.desafio.ingestion.ingestion.service.IngestionRecoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Component;

@Component
public class JobMessageListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobMessageListener.class);
  private final JobOperator jobOperator;
  private final Job job;
  private final IngestionProcessingService processing;
  private final IngestionRecoveryService recovery;

  public JobMessageListener(
      JobOperator jobOperator,
      Job job,
      IngestionProcessingService processing,
      IngestionRecoveryService recovery) {
    this.jobOperator = jobOperator;
    this.job = job;
    this.processing = processing;
    this.recovery = recovery;
  }

  @RabbitListener(queues = INGESTION_QUEUE)
  public void consume(JobMessage message) throws Exception {
    LOGGER.info("event=job_received jobId={}", message.jobId());

    try {
      var ingestion = processing.start(message.jobId());

      var execution =
          jobOperator.start(
              job,
              new JobParametersBuilder()
                  .addString("jobId", message.jobId().toString())
                  .addString(
                      "filePath",
                      java.nio.file.Path.of("/data/uploads", message.jobId() + ".csv").toString())
                  .addString("csvDelimiter", ingestion.getCsvDelimiter())
                  .addString("csvEncoding", ingestion.getCsvEncoding())
                  .addLong("run", System.currentTimeMillis(), false)
                  .toJobParameters());

      if (execution.getStatus() == BatchStatus.FAILED) {
        throw new IllegalStateException("Batch processing failed");
      }

    } catch (Exception exception) {
      recovery.fail(message.jobId(), exception.getMessage());
      LOGGER.error("event=job_processing_error jobId={}", message.jobId(), exception);
      throw exception;
    }
  }
}
