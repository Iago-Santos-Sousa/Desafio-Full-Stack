package com.desafio.ingestion.ingestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.desafio.ingestion.ingestion.application.port.out.CsvStorage;
import com.desafio.ingestion.ingestion.application.port.out.StoredCsv;
import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import com.desafio.ingestion.ingestion.validation.CsvDialect;
import com.desafio.ingestion.ingestion.validation.CsvHeaderValidator;
import com.desafio.ingestion.ingestion.validation.CsvInspection;
import com.desafio.ingestion.ingestion.validation.UploadSizeExceededException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

class IngestionServiceTest {
  private static final long MAX_UPLOAD_BYTES = 2L * 1024 * 1024 * 1024;

  @Test
  void rejectsFileAboveConfiguredLimitBeforeOpeningStream(@TempDir Path uploadDir)
      throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getOriginalFilename()).thenReturn("large.csv");
    when(file.getSize()).thenReturn(MAX_UPLOAD_BYTES + 1);

    IngestionService service = service(uploadDir);

    assertThrows(UploadSizeExceededException.class, () -> service.accept(file));
    verify(file, never()).getInputStream();
  }

  @Test
  void acceptsFileAtConfiguredLimit(@TempDir Path uploadDir) throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getOriginalFilename()).thenReturn("limit.csv");
    when(file.getSize()).thenReturn(MAX_UPLOAD_BYTES);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

    IngestionJobRepository jobs = mock(IngestionJobRepository.class);
    IngestionOutboxService outbox = mock(IngestionOutboxService.class);

    when(jobs.save(any(IngestionJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

    CsvHeaderValidator headers = mock(CsvHeaderValidator.class);

    when(headers.inspect(any(Path.class)))
        .thenReturn(
            new CsvInspection(List.of("column"), new CsvDialect(',', StandardCharsets.UTF_8)));

    CsvStorage storage = mock(CsvStorage.class);
    when(storage.store(any(), any())).thenReturn(new StoredCsv(uploadDir.resolve("job.csv"), 0));

    IngestionService service =
        new IngestionService(jobs, outbox, headers, storage, DataSize.ofBytes(MAX_UPLOAD_BYTES));

    assertDoesNotThrow(() -> service.accept(file));

    verify(file).getInputStream();
    ArgumentCaptor<IngestionJob> jobCaptor = ArgumentCaptor.forClass(IngestionJob.class);
    verify(jobs, org.mockito.Mockito.times(2)).save(jobCaptor.capture());
    assertThat(jobCaptor.getAllValues().get(1).getStatus())
        .isEqualTo(com.desafio.ingestion.ingestion.entity.JobStatus.QUEUED);
    verify(outbox).enqueue(jobCaptor.getAllValues().get(1).getId());
  }

  @Test
  void rejectsNonCsvBeforeOpeningStream(@TempDir Path uploadDir) throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getOriginalFilename()).thenReturn("data.txt");

    IngestionService service = service(uploadDir);

    assertThrows(IllegalArgumentException.class, () -> service.accept(file));
    verify(file, never()).getInputStream();
  }

  private IngestionService service(Path uploadDir) {
    return new IngestionService(
        mock(IngestionJobRepository.class),
        mock(IngestionOutboxService.class),
        mock(CsvHeaderValidator.class),
        mock(CsvStorage.class),
        DataSize.ofBytes(MAX_UPLOAD_BYTES));
  }
}
