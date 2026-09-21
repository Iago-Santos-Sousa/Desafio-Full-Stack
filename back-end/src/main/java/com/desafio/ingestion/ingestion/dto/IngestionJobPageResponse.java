package com.desafio.ingestion.ingestion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Página de jobs com cursor para a próxima página.")
public record IngestionJobPageResponse(List<IngestionJobListItem> items, String nextCursor) {}
