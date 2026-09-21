package com.desafio.ingestion.ingestion.controller;

import com.desafio.ingestion.ingestion.dto.IngestionAcceptedResponse;
import com.desafio.ingestion.ingestion.dto.IngestionJobPageResponse;
import com.desafio.ingestion.ingestion.dto.IngestionJobResponse;
import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.service.IngestionQueryService;
import com.desafio.ingestion.ingestion.service.IngestionService;
import com.desafio.ingestion.shared.ApiExceptionHandler;
import com.desafio.ingestion.shared.OpenApiExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ingestions")
@Tag(name = "Ingestions", description = "Upload, acompanhamento e paginação de jobs CSV.")
public class IngestionController {
  private final IngestionService service;
  private final IngestionQueryService queryService;

  public IngestionController(IngestionService service, IngestionQueryService queryService) {
    this.service = service;
    this.queryService = queryService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(
      summary = "Enviar CSV para ingestão assíncrona",
      description =
          "Recebe um CSV de até 2 GiB, valida o cabeçalho e coloca o job na fila. "
              + "O conteúdo é gravado em streaming e não fica inteiro na memória.",
      operationId = "createIngestion")
  @ApiResponses({
    @ApiResponse(
        responseCode = "202",
        description = "Arquivo aceito e job criado",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = IngestionAcceptedResponse.class),
                examples = @ExampleObject(value = OpenApiExamples.ACCEPTED_INGESTION))),
    @ApiResponse(
        responseCode = "400",
        description = "Parte multipart file ausente ou parâmetro inválido",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.INVALID_REQUEST))),
    @ApiResponse(
        responseCode = "413",
        description = "Arquivo excede o limite de 2 GiB",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.UPLOAD_TOO_LARGE))),
    @ApiResponse(
        responseCode = "422",
        description = "CSV inválido ou cabeçalho incompatível",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.CSV_INVALID))),
    @ApiResponse(
        responseCode = "500",
        description = "Erro interno inesperado",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.INTERNAL_ERROR)))
  })
  public IngestionAcceptedResponse upload(
      @Parameter(
              description = "Arquivo CSV com cabeçalho dinâmico; limite máximo de 2 GiB",
              required = true,
              content =
                  @Content(
                      mediaType = "text/csv",
                      schema = @Schema(type = "string", format = "binary")))
          @RequestPart("file")
          MultipartFile file)
      throws IOException {
    IngestionJob j = service.accept(file);

    return IngestionAcceptedResponse.from(j);
  }

  @GetMapping
  @Operation(
      summary = "Listar jobs de ingestão",
      description = "Retorna jobs em ordem de criação usando paginação keyset por cursor.",
      operationId = "listIngestions")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Página de jobs",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = IngestionJobPageResponse.class),
                examples = @ExampleObject(value = OpenApiExamples.INGESTION_PAGE))),
    @ApiResponse(
        responseCode = "400",
        description = "Cursor inválido",
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
  public IngestionJobPageResponse list(
      @Parameter(
              description = "Quantidade solicitada; limitada entre 1 e 50",
              example = "10",
              schema = @Schema(minimum = "1", maximum = "50", defaultValue = "10"))
          @RequestParam(name = "size", defaultValue = "10")
          int size,
      @Parameter(description = "Cursor opaco retornado pela página anterior")
          @RequestParam(name = "cursor", required = false)
          String cursor) {
    return queryService.list(size, cursor);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Consultar progresso da ingestão",
      description = "Retorna status, contadores, timestamps e erro resumido do job.",
      operationId = "getIngestion")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Detalhes do job",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = IngestionJobResponse.class),
                examples = @ExampleObject(value = OpenApiExamples.INGESTION_DETAIL))),
    @ApiResponse(
        responseCode = "404",
        description = "Job não encontrado",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND))),
    @ApiResponse(
        responseCode = "500",
        description = "Erro interno inesperado",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.INTERNAL_ERROR)))
  })
  public IngestionJobResponse status(
      @Parameter(description = "UUID do job", example = "44a85f6e-6aa6-41ee-a62d-c1a66ee5d206")
          @PathVariable
          UUID id) {
    return IngestionJobResponse.from(service.find(id));
  }

  @GetMapping("/active")
  @Operation(
      summary = "Listar ingestões ativas",
      description = "Retorna jobs RECEIVED, QUEUED ou PROCESSING para atualização por polling.",
      operationId = "listActiveIngestions")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Jobs ativos",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array =
                    @ArraySchema(schema = @Schema(implementation = IngestionJobResponse.class)))),
    @ApiResponse(
        responseCode = "500",
        description = "Erro interno inesperado",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
                examples = @ExampleObject(value = OpenApiExamples.INTERNAL_ERROR)))
  })
  public java.util.List<IngestionJobResponse> active(
      @Parameter(
              description = "Quantidade máxima retornada; limitada entre 1 e 50",
              example = "10",
              schema = @Schema(minimum = "1", maximum = "50", defaultValue = "10"))
          @RequestParam(name = "limit", defaultValue = "10")
          int limit) {
    return service.active(limit).stream().map(IngestionJobResponse::from).toList();
  }
}
