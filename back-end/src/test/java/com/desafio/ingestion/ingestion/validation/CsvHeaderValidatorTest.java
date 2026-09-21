package com.desafio.ingestion.ingestion.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvHeaderValidatorTest {
  private final CsvHeaderValidator validator = new CsvHeaderValidator();

  @Test
  void returnsTrimmedHeadersAndRemovesUtf8Bom(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("headers.csv");
    Files.writeString(file, "\uFEFF name , amount \nAda,10\n");

    assertThat(validator.validate(file)).containsExactly("name", "amount");
  }

  @Test
  void rejectsHeadersThatDifferOnlyByCase(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("duplicate.csv");
    Files.writeString(file, "Name,name\nAda,10\n");

    assertThatThrownBy(() -> validator.validate(file))
        .isInstanceOf(CsvFormatException.class)
        .extracting("code")
        .isEqualTo("CSV_HEADER_INVALID");
  }

  @Test
  void rejectsBlankHeaders(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("blank.csv");
    Files.writeString(file, "name, \nAda,10\n");

    assertThatThrownBy(() -> validator.validate(file))
        .isInstanceOf(CsvFormatException.class)
        .extracting("code")
        .isEqualTo("CSV_HEADER_INVALID");
  }

  @Test
  void rejectsMoreThanMaximumColumns(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("too-many-columns.csv");
    String headers = String.join(",", java.util.Collections.nCopies(257, "column"));

    Files.writeString(
        file, headers + "\n" + String.join(",", java.util.Collections.nCopies(257, "value")));

    assertThatThrownBy(() -> validator.validate(file))
        .isInstanceOf(CsvFormatException.class)
        .hasMessageContaining("256");
  }

  @Test
  void detectsSemicolonDelimiter(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("semicolon.csv");
    Files.writeString(file, "name;amount\nAda;10\n");

    CsvInspection inspection = validator.inspect(file);

    assertThat(inspection.headers()).containsExactly("name", "amount");
    assertThat(inspection.dialect().delimiter()).isEqualTo(';');
    assertThat(inspection.dialect().charset()).isEqualTo(StandardCharsets.UTF_8);
  }

  @Test
  void detectsUtf16LittleEndianWithBom(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("utf16.csv");
    Files.write(file, ("name,amount\nAda,10\n").getBytes(StandardCharsets.UTF_16LE));

    byte[] content = Files.readAllBytes(file);
    byte[] withBom = new byte[content.length + 2];
    withBom[0] = (byte) 0xFF;
    withBom[1] = (byte) 0xFE;

    System.arraycopy(content, 0, withBom, 2, content.length);
    Files.write(file, withBom);

    CsvInspection inspection = validator.inspect(file);

    assertThat(inspection.headers()).containsExactly("name", "amount");
    assertThat(inspection.dialect().charset()).isEqualTo(StandardCharsets.UTF_16LE);
  }

  @Test
  void rejectsInvalidUtf8(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("invalid-encoding.csv");
    Files.write(file, new byte[] {(byte) 0xC3, (byte) 0x28});

    assertThatThrownBy(() -> validator.inspect(file))
        .isInstanceOf(CsvFormatException.class)
        .extracting("code")
        .isEqualTo("CSV_ENCODING_UNSUPPORTED");
  }

  @Test
  void rejectsEmptyFileAsInvalidHeader(@TempDir Path directory) throws Exception {
    Path file = directory.resolve("empty.csv");
    Files.createFile(file);

    assertThatThrownBy(() -> validator.inspect(file))
        .isInstanceOf(CsvFormatException.class)
        .extracting("code")
        .isEqualTo("CSV_HEADER_INVALID");
  }
}
