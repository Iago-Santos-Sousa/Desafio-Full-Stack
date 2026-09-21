package com.desafio.ingestion.ingestion.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UploadPolicyTest {
  @Test
  void rejectsNonCsvFilenameBeforeOpeningContent() {
    assertThatThrownBy(() -> UploadPolicy.validateCsv("data.txt", 10, 100))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("CSV file is required");
  }

  @Test
  void rejectsFilesAboveConfiguredLimit() {
    assertThatThrownBy(() -> UploadPolicy.validateCsv("data.csv", 101, 100))
        .isInstanceOf(UploadSizeExceededException.class);
  }
}
