package com.desafio.ingestion.ingestion.repository;

import com.desafio.ingestion.ingestion.cursor.IngestionCursorCodec;
import com.desafio.ingestion.ingestion.dto.IngestionJobListItem;
import com.desafio.ingestion.ingestion.entity.IngestionJob;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface IngestionJobQueryRepository extends Repository<IngestionJob, UUID> {
  @Query(
      "select new com.desafio.ingestion.ingestion.dto.IngestionJobListItem("
          + "j.id, j.originalFilename, j.fileSizeBytes, j.status, j.createdAt, j.updatedAt)"
          + " from IngestionJob j order by j.createdAt desc, j.id desc")
  List<IngestionJobListItem> findFirstPage(Limit limit);

  @Query(
      "select new com.desafio.ingestion.ingestion.dto.IngestionJobListItem("
          + "j.id, j.originalFilename, j.fileSizeBytes, j.status, j.createdAt, j.updatedAt)"
          + " from IngestionJob j"
          + " where j.createdAt < :createdAt"
          + " or (j.createdAt = :createdAt and j.id < :jobId)"
          + " order by j.createdAt desc, j.id desc")
  List<IngestionJobListItem> findPageAfter(
      @Param("createdAt") Instant createdAt, @Param("jobId") UUID jobId, Limit limit);

  default List<IngestionJobListItem> findPage(int limit, IngestionCursorCodec.Cursor cursor) {
    Limit pageLimit = Limit.of(limit + 1);

    return cursor == null
        ? findFirstPage(pageLimit)
        : findPageAfter(cursor.createdAt(), cursor.jobId(), pageLimit);
  }
}
