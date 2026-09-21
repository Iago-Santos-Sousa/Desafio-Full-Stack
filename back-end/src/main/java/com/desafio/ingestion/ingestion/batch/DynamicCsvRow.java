package com.desafio.ingestion.ingestion.batch;

import java.util.UUID;

public record DynamicCsvRow(UUID jobId, long rowNumber, String dataJson) {}
