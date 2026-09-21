package com.desafio.ingestion.analytics.controller;

import com.desafio.ingestion.analytics.dto.AggregateDto;
import com.desafio.ingestion.analytics.dto.SummaryDto;
import com.desafio.ingestion.analytics.service.AnalyticsService;
import com.desafio.ingestion.shared.ApiExceptionHandler;
import com.desafio.ingestion.shared.OpenApiExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Métricas agregadas usadas pelo dashboard.")
public class AnalyticsController {
  private final AnalyticsService service;

  public AnalyticsController(AnalyticsService service) {
    this.service = service;
  }

  @GetMapping("/summary")
  @Operation(
      summary = "Consultar resumo do dashboard",
      description =
          "Retorna registros processados, linhas válidas e documentos no intervalo inclusivo.",
      operationId = "getAnalyticsSummary")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Resumo agregado",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SummaryDto.class),
                examples = @ExampleObject(value = OpenApiExamples.SUMMARY))),
    @ApiResponse(
        responseCode = "400",
        description = "Intervalo de datas inválido",
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
  public SummaryDto summary(
      @Parameter(
              description = "Data inicial inclusiva no fuso America/Sao_Paulo",
              example = "2026-09-01",
              schema = @Schema(type = "string", format = "date"))
          @RequestParam(name = "from", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate from,
      @Parameter(
              description = "Data final inclusiva no fuso America/Sao_Paulo",
              example = "2026-09-20",
              schema = @Schema(type = "string", format = "date"))
          @RequestParam(name = "to", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate to) {
    return service.summary(from, to);
  }

  @GetMapping("/monthly")
  @Operation(
      summary = "Consultar registros processados por mês",
      description = "Retorna agregação mensal dentro do intervalo inclusivo informado.",
      operationId = "getMonthlyAnalytics")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Agregação mensal",
        content =
            @Content(
                mediaType = "application/json",
                array =
                    @io.swagger.v3.oas.annotations.media.ArraySchema(
                        schema = @Schema(implementation = AggregateDto.class)),
                examples = @ExampleObject(value = OpenApiExamples.MONTHLY))),
    @ApiResponse(
        responseCode = "400",
        description = "Intervalo de datas inválido",
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
  public List<AggregateDto> monthly(
      @Parameter(
              description = "Data inicial inclusiva no fuso America/Sao_Paulo",
              example = "2026-09-01",
              schema = @Schema(type = "string", format = "date"))
          @RequestParam(name = "from", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate from,
      @Parameter(
              description = "Data final inclusiva no fuso America/Sao_Paulo",
              example = "2026-09-20",
              schema = @Schema(type = "string", format = "date"))
          @RequestParam(name = "to", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate to) {
    return service.monthlyAggregates(from, to);
  }
}
