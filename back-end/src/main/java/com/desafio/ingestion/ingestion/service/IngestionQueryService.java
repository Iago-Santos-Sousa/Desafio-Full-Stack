package com.desafio.ingestion.ingestion.service;

import com.desafio.ingestion.ingestion.cursor.IngestionCursorCodec;
import com.desafio.ingestion.ingestion.dto.IngestionJobListItem;
import com.desafio.ingestion.ingestion.dto.IngestionJobPageResponse;
import com.desafio.ingestion.ingestion.repository.IngestionJobQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionQueryService {
  private final IngestionJobQueryRepository repository;

  public IngestionQueryService(IngestionJobQueryRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public IngestionJobPageResponse list(int requestedSize, String cursorValue) {
    int size = Math.min(Math.max(requestedSize, 1), 50);

    List<IngestionJobListItem> rows =
        repository.findPage(size, IngestionCursorCodec.decode(cursorValue));

    boolean hasMore = rows.size() > size;

    if (hasMore) {
      rows = rows.subList(0, size);
    }

    String nextCursor =
        hasMore && !rows.isEmpty() ? IngestionCursorCodec.encode(rows.get(rows.size() - 1)) : null;

    return new IngestionJobPageResponse(rows, nextCursor);
  }
}
