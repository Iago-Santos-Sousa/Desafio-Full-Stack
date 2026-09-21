package com.desafio.ingestion.transaction.repository;

import com.desafio.ingestion.transaction.entity.CsvRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<CsvRecord, Long>, RecordRepositoryCustom {}
