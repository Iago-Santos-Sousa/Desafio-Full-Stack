package com.desafio.ingestion.ingestion.batch;

import com.desafio.ingestion.ingestion.service.IngestionProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@EnableJdbcJobRepository
public class BatchConfig {
  @Bean
  ObjectMapper objectMapper() {
    return JsonMapper.builder().build();
  }

  @Bean
  @StepScope
  DynamicCsvItemReader csvReader(
      @Value("#{jobParameters['filePath']}") String filePath,
      @Value("#{jobParameters['jobId']}") String jobId,
      @Value("#{jobParameters['csvDelimiter'] ?: ','}") String delimiter,
      @Value("#{jobParameters['csvEncoding'] ?: 'UTF-8'}") String encoding,
      ObjectMapper objectMapper,
      IngestionProgressTracker progress) {
    return new DynamicCsvItemReader(
        Path.of(filePath),
        UUID.fromString(jobId),
        objectMapper,
        delimiter.charAt(0),
        java.nio.charset.Charset.forName(encoding),
        progress);
  }

  @Bean
  @StepScope
  IngestionProgressTracker ingestionProgressTracker(
      @Value("#{jobParameters['jobId']}") String jobId) {
    IngestionProgressTracker tracker = new IngestionProgressTracker();
    tracker.initialize(UUID.fromString(jobId), 0, 0, 0);
    return tracker;
  }

  @Bean
  @StepScope
  IngestionProgressWriter csvWriter(
      javax.sql.DataSource dataSource,
      IngestionProgressTracker progressTracker,
      IngestionProgressService progressService) {
    JdbcBatchItemWriter<DynamicCsvRow> delegate =
        new JdbcBatchItemWriterBuilder<DynamicCsvRow>()
            .dataSource(dataSource)
            .sql(
                "INSERT INTO csv_record"
                    + " (ingestion_job_id,row_number,data) VALUES (?,?,CAST(? AS jsonb))"
                    + " ON CONFLICT (ingestion_job_id,row_number) DO NOTHING")
            .itemPreparedStatementSetter(
                (item, ps) -> {
                  ps.setObject(1, item.jobId());
                  ps.setLong(2, item.rowNumber());
                  ps.setString(3, item.dataJson());
                })
            .build();

    return new IngestionProgressWriter(delegate, progressTracker, progressService);
  }

  @Bean
  Step ingestionStep(
      JobRepository repo,
      PlatformTransactionManager tx,
      DynamicCsvItemReader csvReader,
      ItemWriter<DynamicCsvRow> csvWriter,
      IngestionJobListener listener,
      IngestionProgressChunkListener progressListener) {
    return new StepBuilder("ingestionStep", repo)
        .<DynamicCsvRow, DynamicCsvRow>chunk(2000)
        .transactionManager(tx)
        .reader(csvReader)
        .writer(csvWriter)
        .faultTolerant()
        .skip(IllegalArgumentException.class)
        .skipLimit(10000)
        .listener(listener)
        .listener(progressListener)
        .build();
  }

  @Bean
  Job ingestionJob(JobRepository repo, Step ingestionStep, IngestionJobListener listener) {
    return new JobBuilder("ingestionJob", repo).listener(listener).start(ingestionStep).build();
  }
}
