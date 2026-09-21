package com.desafio.ingestion.ingestion.application.port.out;

import java.nio.file.Path;

public record StoredCsv(Path path, long sizeBytes) {}
