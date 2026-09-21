package com.desafio.ingestion.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Quantidade de registros processados por mês.")
public record AggregateDto(LocalDate month, long recordCount) {}
