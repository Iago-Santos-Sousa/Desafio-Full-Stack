package com.desafio.ingestion.shared;

import com.desafio.ingestion.analytics.service.InvalidDateRangeException;
import com.desafio.ingestion.ingestion.cursor.InvalidCursorException;
import com.desafio.ingestion.ingestion.validation.CsvFormatException;
import com.desafio.ingestion.ingestion.validation.UploadSizeExceededException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(NoSuchElementException.class)
  ResponseEntity<ApiProblem> notFound(
      NoSuchElementException exception, HttpServletRequest request) {
    return problem(404, "NOT_FOUND", "Resource not found", exception, request);
  }

  @ExceptionHandler(CsvFormatException.class)
  ResponseEntity<ApiProblem> csvFormat(CsvFormatException exception, HttpServletRequest request) {
    return problem(422, exception.getCode(), "Invalid CSV", exception, request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ApiProblem> invalid(
      IllegalArgumentException exception, HttpServletRequest request) {
    return problem(422, "INVALID_REQUEST", "Invalid request", exception, request);
  }

  @ExceptionHandler(InvalidDateRangeException.class)
  ResponseEntity<ApiProblem> invalidDateRange(
      InvalidDateRangeException exception, HttpServletRequest request) {
    return problem(400, "INVALID_DATE_RANGE", "Invalid date range", exception, request);
  }

  @ExceptionHandler(InvalidCursorException.class)
  ResponseEntity<ApiProblem> invalidCursor(
      InvalidCursorException exception, HttpServletRequest request) {
    return problem(400, "INVALID_CURSOR", "Invalid cursor", exception, request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ApiProblem> parameterMismatch(
      MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
    return problem(400, "INVALID_PARAMETER", "Invalid parameter", exception, request);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<ApiProblem> uploadTooLarge(
      MaxUploadSizeExceededException exception, HttpServletRequest request) {
    return problem(413, "UPLOAD_TOO_LARGE", "Upload too large", exception, request);
  }

  @ExceptionHandler(UploadSizeExceededException.class)
  ResponseEntity<ApiProblem> uploadSizeExceeded(
      UploadSizeExceededException exception, HttpServletRequest request) {
    return problem(413, "UPLOAD_TOO_LARGE", "Upload too large", exception, request);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  ResponseEntity<ApiProblem> missingParameter(
      MissingServletRequestParameterException exception, HttpServletRequest request) {
    return problem(400, "MISSING_PARAMETER", "Missing parameter", exception, request);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiProblem> internal(Exception exception, HttpServletRequest request) {
    String trace = traceId(request);

    LOGGER.error(
        "event=unhandled_exception method={} path={} traceId={}",
        request.getMethod(),
        request.getRequestURI(),
        trace,
        exception);

    ApiProblem body =
        new ApiProblem(
            500,
            "INTERNAL_ERROR",
            "Internal server error",
            "Unexpected server error.",
            Instant.now(),
            trace);

    return ResponseEntity.status(500).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
  }

  private ResponseEntity<ApiProblem> problem(
      int status, String code, String title, Exception exception, HttpServletRequest request) {
    String detail = exception.getMessage() == null ? title : exception.getMessage();

    LOGGER.warn(
        "event=api_error status={} code={} method={} path={} traceId={} detail={}",
        status,
        code,
        request.getMethod(),
        request.getRequestURI(),
        traceId(request),
        detail);

    ApiProblem body = new ApiProblem(status, code, title, detail, Instant.now(), traceId(request));

    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
  }

  private String traceId(HttpServletRequest request) {
    Object value = request.getAttribute(TraceIdFilter.REQUEST_ATTRIBUTE);
    return value == null ? "unknown" : value.toString();
  }

  @Schema(description = "Erro HTTP no formato Problem Details da API.")
  public record ApiProblem(
      int status, String code, String title, String detail, Instant timestamp, String traceId) {}
}
