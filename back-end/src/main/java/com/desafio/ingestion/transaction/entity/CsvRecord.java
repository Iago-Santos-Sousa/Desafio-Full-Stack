package com.desafio.ingestion.transaction.entity;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "csv_record")
public class CsvRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ingestion_job_id", nullable = false)
  private IngestionJob ingestionJob;

  @Column(name = "row_number", nullable = false)
  private long rowNumber;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "data", nullable = false, columnDefinition = "jsonb")
  private Map<String, String> data;

  protected CsvRecord() {}

  public Long getId() {
    return id;
  }

  public IngestionJob getIngestionJob() {
    return ingestionJob;
  }

  public long getRowNumber() {
    return rowNumber;
  }

  public Map<String, String> getData() {
    return data;
  }
}
