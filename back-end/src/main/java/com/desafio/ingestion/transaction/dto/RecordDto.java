package com.desafio.ingestion.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Linha de CSV com valores dinâmicos preservados como texto.")
public record RecordDto(long id, long rowNumber, Map<String, String> values) {}
