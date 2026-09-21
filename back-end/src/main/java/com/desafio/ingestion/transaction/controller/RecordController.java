package com.desafio.ingestion.transaction.controller;

import com.desafio.ingestion.shared.ApiExceptionHandler;
import com.desafio.ingestion.shared.OpenApiExamples;
import com.desafio.ingestion.transaction.dto.RecordPageResponse;
import com.desafio.ingestion.transaction.service.RecordQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingestions/{jobId}/records")
@Tag(name = "CSV records", description = "Consulta paginada de linhas CSV dinâmicas.")
public class RecordController {
  private final RecordQueryService service;

  public RecordController(RecordQueryService service) {
    this.service = service;
  }

  @GetMapping
  @Operation(
      summary = "Listar registros de um job",
      description =
          "Retorna colunas e linhas dinâmicas usando cursor keyset. A resposta inclui totalRecords"
              + " e totalPages derivados do contador de linhas válidas confirmadas no job; não"
              + " executa COUNT(*) na tabela de registros. Não há filtro por categoria no contrato"
              + " atual. Jobs com status FAILED também podem retornar registros válidos persistidos"
              + " antes da falha; nesse caso, a página representa dados parciais.",
      operationId = "listIngestionRecords")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Página de registros com totais do job",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RecordPageResponse.class),
                examples = {
                  @ExampleObject(
                      name = "registrosProcessados",
                      summary = "Registros processados",
                      value = OpenApiExamples.RECORD_PAGE),
                  @ExampleObject(
                      name = "registrosParciaisJobFalho",
                      summary = "Registros persistidos antes de falha",
                      description =
                          "Exemplo de resposta 200 para job FAILED. "
                              + "O status e os contadores do job são consultados "
                              + "em GET /api/v1/ingestions/{jobId}; esta página "
                              + "contém somente registros válidos persistidos antes da falha.",
                      value = OpenApiExamples.RECORD_PAGE_FAILED_PARTIAL)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "Job não encontrado",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND))),
    @ApiResponse(
        responseCode = "400",
        description = "Cursor ou tamanho inválido",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.INVALID_REQUEST))),
    @ApiResponse(
        responseCode = "500",
        description = "Erro interno inesperado",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.INTERNAL_ERROR)))
  })
  public RecordPageResponse list(
      @Parameter(description = "UUID do job", example = "44a85f6e-6aa6-41ee-a62d-c1a66ee5d206")
          @PathVariable
          UUID jobId,
      @Parameter(
              description = "Quantidade solicitada; limitada entre 1 e 200",
              example = "25",
              schema = @Schema(minimum = "1", maximum = "200", defaultValue = "25"))
          @RequestParam(name = "size", defaultValue = "25")
          int size,
      @Parameter(
              description = "ID do último registro retornado pela página anterior",
              example = "25")
          @RequestParam(name = "cursor", required = false)
          Long cursor) {
    return service.list(jobId, size, cursor);
  }
}
