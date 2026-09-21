package com.desafio.ingestion.shared;

public final class OpenApiExamples {
  public static final String ACCEPTED_INGESTION =
      """
      {
        "jobId": "44a85f6e-6aa6-41ee-a62d-c1a66ee5d206",
        "status": "QUEUED",
        "statusUrl": "/api/v1/ingestions/44a85f6e-6aa6-41ee-a62d-c1a66ee5d206"
      }
      """;

  public static final String INGESTION_DETAIL =
      """
      {
        "jobId": "44a85f6e-6aa6-41ee-a62d-c1a66ee5d206",
        "originalFilename": "customers.csv",
        "status": "COMPLETED",
        "totalRows": 2000000,
        "processedRows": 2000000,
        "validRows": 1999987,
        "invalidRows": 13,
        "columns": ["name", "email", "company"],
        "fileSizeBytes": 341228000,
        "createdAt": "2026-09-20T12:00:00Z",
        "queuedAt": "2026-09-20T12:00:01Z",
        "startedAt": "2026-09-20T12:00:02Z",
        "finishedAt": "2026-09-20T12:03:42Z",
        "updatedAt": "2026-09-20T12:03:42Z",
        "errorSummary": null
      }
      """;

  public static final String INGESTION_PAGE =
      """
      {
        "items": [{
          "jobId": "44a85f6e-6aa6-41ee-a62d-c1a66ee5d206",
          "originalFilename": "customers.csv",
          "fileSizeBytes": 341228000,
          "status": "COMPLETED",
          "createdAt": "2026-09-20T12:00:00Z",
          "updatedAt": "2026-09-20T12:03:42Z"
        }],
        "nextCursor": "eyJjcmVhdGVkQXQiOiIyMDI2LTA5LTIwVDEyOjAwOjAwWiJ9"
      }
      """;

  public static final String RECORD_PAGE =
      """
      {
        "columns": ["name", "email", "company"],
        "items": [{
          "id": 1,
          "rowNumber": 2,
          "values": {
            "name": "Ada Lovelace",
            "email": "ada@example.com",
            "company": "Analytical Engines"
          }
        }],
        "nextCursor": 1,
        "totalRecords": 1,
        "totalPages": 1
      }
      """;

  public static final String RECORD_PAGE_FAILED_PARTIAL =
      """
      {
        "columns": ["name", "email", "company"],
        "items": [{
          "id": 1,
          "rowNumber": 2,
          "values": {
            "name": "Ada Lovelace",
            "email": "ada@example.com",
            "company": "Analytical Engines"
          }
        }],
        "nextCursor": null,
        "totalRecords": 1,
        "totalPages": 1
      }
      """;

  public static final String SUMMARY =
      """
      {
        "recordCount": 2000000,
        "validRowCount": 1999987,
        "documentCount": 3
      }
      """;

  public static final String MONTHLY =
      """
      [{"month": "2026-09-01", "recordCount": 2000000}]
      """;

  public static final String INVALID_REQUEST =
      """
      {
        "status": 400,
        "code": "INVALID_DATE_RANGE",
        "title": "Invalid date range",
        "detail": "The start date must be before or equal to the end date.",
        "timestamp": "2026-09-20T12:03:42Z",
        "traceId": "7f2d7f0c9e8f4a3b"
      }
      """;

  public static final String NOT_FOUND =
      """
      {
        "status": 404,
        "code": "NOT_FOUND",
        "title": "Resource not found",
        "detail": "Ingestion job not found",
        "timestamp": "2026-09-20T12:03:42Z",
        "traceId": "7f2d7f0c9e8f4a3b"
      }
      """;

  public static final String CSV_INVALID =
      """
      {
        "status": 422,
        "code": "CSV_HEADER_INVALID",
        "title": "Invalid CSV",
        "detail": "CSV headers must be non-empty and unique.",
        "timestamp": "2026-09-20T12:03:42Z",
        "traceId": "7f2d7f0c9e8f4a3b"
      }
      """;

  public static final String UPLOAD_TOO_LARGE =
      """
      {
        "status": 413,
        "code": "UPLOAD_TOO_LARGE",
        "title": "Upload too large",
        "detail": "CSV file exceeds the 2 GiB limit.",
        "timestamp": "2026-09-20T12:03:42Z",
        "traceId": "7f2d7f0c9e8f4a3b"
      }
      """;

  public static final String INTERNAL_ERROR =
      """
      {
        "status": 500,
        "code": "INTERNAL_ERROR",
        "title": "Internal server error",
        "detail": "Unexpected server error.",
        "timestamp": "2026-09-20T12:03:42Z",
        "traceId": "7f2d7f0c9e8f4a3b"
      }
      """;

  private OpenApiExamples() {}
}
