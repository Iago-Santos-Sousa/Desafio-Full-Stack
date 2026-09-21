package com.desafio.ingestion.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Métricas agregadas do período consultado.")
public record SummaryDto(long recordCount, long validRowCount, long documentCount) {}
