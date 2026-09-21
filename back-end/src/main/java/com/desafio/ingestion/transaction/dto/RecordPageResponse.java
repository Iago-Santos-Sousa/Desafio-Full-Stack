package com.desafio.ingestion.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Página de registros dinâmicos de um job.")
public record RecordPageResponse(
    List<String> columns,
    List<RecordDto> items,
    Long nextCursor,
    @Schema(description = "Quantidade de registros válidos confirmados para o job.")
        long totalRecords,
    @Schema(description = "Quantidade de páginas para o tamanho solicitado.") long totalPages) {}
