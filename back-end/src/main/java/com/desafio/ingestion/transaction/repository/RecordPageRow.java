package com.desafio.ingestion.transaction.repository;

import java.util.Map;

public record RecordPageRow(Long id, long rowNumber, Map<String, String> values) {}
