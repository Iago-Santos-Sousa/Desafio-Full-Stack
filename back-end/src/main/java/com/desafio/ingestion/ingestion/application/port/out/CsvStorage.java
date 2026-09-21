package com.desafio.ingestion.ingestion.application.port.out;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

public interface CsvStorage {
  StoredCsv store(UUID jobId, InputStream content) throws IOException;

  void delete(Path path) throws IOException;
}
