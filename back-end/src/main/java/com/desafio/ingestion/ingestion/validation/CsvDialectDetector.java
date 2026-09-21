package com.desafio.ingestion.ingestion.validation;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

public class CsvDialectDetector {
  private static final int SAMPLE_SIZE = 64 * 1024;
  private static final char[] CANDIDATES = {',', ';', '\t', '|'};

  public CsvDialect detect(Path file) throws IOException {
    byte[] sample = readSample(file);
    DetectedEncoding detectedEncoding = detectEncoding(sample);
    String text = decode(sample, detectedEncoding);
    String completeSample = completeRecords(text);

    return new CsvDialect(selectDelimiter(completeSample), detectedEncoding.charset());
  }

  private byte[] readSample(Path file) throws IOException {
    try (var input = Files.newInputStream(file)) {
      byte[] sample = new byte[SAMPLE_SIZE];
      int offset = 0;
      int read;

      while (offset < sample.length
          && (read = input.read(sample, offset, sample.length - offset)) > 0) {
        offset += read;
      }

      return java.util.Arrays.copyOf(sample, offset);
    }
  }

  private DetectedEncoding detectEncoding(byte[] sample) {
    if (startsWith(sample, (byte) 0xEF, (byte) 0xBB, (byte) 0xBF)) {
      return new DetectedEncoding(StandardCharsets.UTF_8, 3);
    }

    if (startsWith(sample, (byte) 0xFF, (byte) 0xFE)) {
      return new DetectedEncoding(StandardCharsets.UTF_16LE, 2);
    }

    if (startsWith(sample, (byte) 0xFE, (byte) 0xFF)) {
      return new DetectedEncoding(StandardCharsets.UTF_16BE, 2);
    }

    return new DetectedEncoding(StandardCharsets.UTF_8, 0);
  }

  private String decode(byte[] sample, DetectedEncoding encoding) {
    int offset = encoding.bomLength();
    int length = sample.length - offset;

    if (length <= 0) {
      throw new CsvFormatException("CSV_ENCODING_UNSUPPORTED", "CSV has no decodable content");
    }

    if (encoding.charset().equals(StandardCharsets.UTF_16LE)
        || encoding.charset().equals(StandardCharsets.UTF_16BE)) {
      length -= length % 2;
    }

    int minimumLength = Math.max(0, length - 4);

    for (int candidateLength = length; candidateLength >= minimumLength; candidateLength--) {
      if ((encoding.charset().equals(StandardCharsets.UTF_16LE)
              || encoding.charset().equals(StandardCharsets.UTF_16BE))
          && candidateLength % 2 != 0) {
        continue;
      }

      try {
        var decoder = encoding.charset().newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        CharBuffer decoded = decoder.decode(ByteBuffer.wrap(sample, offset, candidateLength));
        return decoded.toString();
      } catch (CharacterCodingException exception) {
        // Uma amostra delimitada pode terminar no meio de um caractere multibyte.
      }
    }

    throw new CsvFormatException(
        "CSV_ENCODING_UNSUPPORTED", "CSV encoding is invalid or unsupported");
  }

  private String completeRecords(String text) {
    int lastNewline = Math.max(text.lastIndexOf('\n'), text.lastIndexOf('\r'));

    if (lastNewline < 0) {
      return text;
    }

    return text.substring(0, lastNewline + 1);
  }

  private char selectDelimiter(String sample) {
    char selected = ',';
    int bestScore = 0;
    int bestColumns = 1;

    for (char candidate : CANDIDATES) {
      List<Integer> sizes = recordSizes(sample, candidate);

      if (sizes.isEmpty()) {
        continue;
      }

      int columns = sizes.get(0);

      if (columns < 2) {
        continue;
      }

      int score = (int) sizes.stream().filter(size -> size == columns).count();

      if (score > bestScore || (score == bestScore && columns > bestColumns)) {
        selected = candidate;
        bestScore = score;
        bestColumns = columns;
      }
    }

    return selected;
  }

  private List<Integer> recordSizes(String sample, char delimiter) {
    try (CSVParser parser =
        CSVFormat.RFC4180.builder().setDelimiter(delimiter).get().parse(new StringReader(sample))) {
      List<Integer> sizes = new ArrayList<>();

      parser.stream().limit(8).forEach(record -> sizes.add(record.size()));
      return sizes;
    } catch (IOException | IllegalArgumentException exception) {
      return List.of();
    }
  }

  private boolean startsWith(byte[] value, byte... prefix) {
    if (value.length < prefix.length) {
      return false;
    }

    for (int index = 0; index < prefix.length; index++) {
      if (value[index] != prefix[index]) {
        return false;
      }
    }

    return true;
  }

  private record DetectedEncoding(Charset charset, int bomLength) {}
}
