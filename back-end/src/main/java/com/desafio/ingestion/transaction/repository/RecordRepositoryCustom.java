package com.desafio.ingestion.transaction.repository;

import java.util.List;
import java.util.UUID;

public interface RecordRepositoryCustom {
  List<RecordPageRow> findPage(int limit, Long cursor, UUID jobId);
}
