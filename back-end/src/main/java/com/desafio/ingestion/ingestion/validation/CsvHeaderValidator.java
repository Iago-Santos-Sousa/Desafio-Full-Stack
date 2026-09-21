package com.desafio.ingestion.ingestion.validation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Component;

@Component
public class CsvHeaderValidator {
  public static final int MAX_COLUMNS = 256;
  public static final int MAX_COLUMN_NAME_LENGTH = 128;

  private final CsvDialectDetector dialectDetector;

  public CsvHeaderValidator() {
    this(new CsvDialectDetector());
  }

  CsvHeaderValidator(CsvDialectDetector dialectDetector) {
    this.dialectDetector = dialectDetector;
  }

  public List<String> validate(Path file) throws IOException {
    return inspect(file).headers();
  }

  public CsvInspection inspect(Path file) throws IOException {
    if (Files.size(file) == 0) {
      throw new CsvFormatException("CSV_HEADER_INVALID", "CSV must contain a header");
    }

    CsvDialect dialect = dialectDetector.detect(file);

    try (Reader reader = strictReader(file, dialect.charset());
        CSVParser parser = parser(reader, dialect.delimiter())) {
      List<String> headers =
          parser.getHeaderNames().stream().map(this::stripBom).map(String::trim).toList();

      validateHeaders(headers);

      return new CsvInspection(headers, dialect);
    } catch (CsvFormatException exception) {
      throw exception;
    } catch (CharacterCodingException exception) {
      throw new CsvFormatException(
          "CSV_ENCODING_UNSUPPORTED", "CSV encoding is invalid or unsupported");
    } catch (IllegalArgumentException exception) {
      throw new CsvFormatException("CSV_HEADER_INVALID", exception.getMessage());
    }
  }

  private CSVParser parser(Reader reader, char delimiter) throws IOException {
    return CSVFormat.RFC4180
        .builder()
        .setDelimiter(delimiter)
        .setHeader()
        .setSkipHeaderRecord(true)
        .get()
        .parse(reader);
  }

  private Reader strictReader(Path file, java.nio.charset.Charset charset) throws IOException {
    var decoder = charset.newDecoder();
    decoder.onMalformedInput(CodingErrorAction.REPORT);
    decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
    return new InputStreamReader(Files.newInputStream(file), decoder);
  }

  private void validateHeaders(List<String> headers) {
    if (headers.isEmpty() || headers.size() > MAX_COLUMNS) {
      throw new IllegalArgumentException("CSV must contain between 1 and 256 columns");
    }

    Set<String> normalized = new HashSet<>();

    for (String header : headers) {
      if (header.isBlank()
          || header.length() > MAX_COLUMN_NAME_LENGTH
          || !normalized.add(header.toLowerCase(Locale.ROOT))) {
        throw new IllegalArgumentException("CSV headers must be non-empty and unique");
      }
    }
  }

  private String stripBom(String value) {
    return value.startsWith("\uFEFF") ? value.substring(1) : value;
  }
}
