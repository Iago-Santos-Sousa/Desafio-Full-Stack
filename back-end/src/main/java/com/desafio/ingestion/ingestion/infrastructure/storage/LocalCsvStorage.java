package com.desafio.ingestion.ingestion.infrastructure.storage;

import com.desafio.ingestion.ingestion.application.port.out.CsvStorage;
import com.desafio.ingestion.ingestion.application.port.out.StoredCsv;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalCsvStorage implements CsvStorage {
  private final Path uploadDir;

  public LocalCsvStorage(@Value("${app.upload-dir:/data/uploads}") String directory) {
    this.uploadDir = Path.of(directory);
  }

  @Override
  public StoredCsv store(UUID jobId, InputStream content) throws IOException {
    Files.createDirectories(uploadDir);
    Path target = uploadDir.resolve(jobId + ".csv");

    try (OutputStream output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
      content.transferTo(output);
      return new StoredCsv(target, Files.size(target));
    } catch (IOException | RuntimeException exception) {
      Files.deleteIfExists(target);
      throw exception;
    }
  }

  @Override
  public void delete(Path path) throws IOException {
    if (path != null) {
      Files.deleteIfExists(path);
    }
  }
}
