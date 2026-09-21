package com.desafio.ingestion.transaction.repository;

import com.desafio.ingestion.transaction.entity.CsvRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecordRepositoryImpl implements RecordRepositoryCustom {
  private final EntityManager entityManager;

  public RecordRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<RecordPageRow> findPage(int limit, Long cursor, UUID jobId) {
    List<Long> pageIds = findPageIds(limit, cursor, jobId);

    if (pageIds.isEmpty()) {
      return List.of();
    }

    Map<Long, RecordPageRow> rowsById =
        findRowsByIds(pageIds, jobId).stream()
            .collect(Collectors.toMap(RecordPageRow::id, Function.identity()));

    return pageIds.stream().map(rowsById::get).filter(Objects::nonNull).toList();
  }

  private List<Long> findPageIds(int limit, Long cursor, UUID jobId) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);
    Root<CsvRecord> record = query.from(CsvRecord.class);
    List<Predicate> predicates = new ArrayList<>();

    if (cursor != null) {
      predicates.add(builder.greaterThan(record.<Long>get("id"), cursor));
    }

    predicates.add(builder.equal(record.get("ingestionJob").get("id"), jobId));

    query
        .select(record.get("id"))
        .where(predicates.toArray(Predicate[]::new))
        .orderBy(builder.asc(record.get("id")));

    return entityManager.createQuery(query).setMaxResults(limit + 1).getResultList();
  }

  private List<RecordPageRow> findRowsByIds(List<Long> ids, UUID jobId) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<RecordPageRow> query = builder.createQuery(RecordPageRow.class);
    Root<CsvRecord> record = query.from(CsvRecord.class);

    query
        .select(
            builder.construct(
                RecordPageRow.class, record.get("id"), record.get("rowNumber"), record.get("data")))
        .where(
            builder.and(
                record.get("id").in(ids),
                builder.equal(record.get("ingestionJob").get("id"), jobId)))
        .orderBy(builder.asc(record.get("id")));

    return entityManager.createQuery(query).getResultList();
  }
}
