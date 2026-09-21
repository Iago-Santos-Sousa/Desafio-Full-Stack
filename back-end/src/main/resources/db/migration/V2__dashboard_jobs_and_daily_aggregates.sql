CREATE TABLE daily_job_category_aggregate
(
    ingestion_job_id  UUID           NOT NULL REFERENCES ingestion_job (id),
    day               DATE           NOT NULL,
    category          VARCHAR(120)   NOT NULL,
    total_amount      NUMERIC(24, 4) NOT NULL,
    transaction_count BIGINT         NOT NULL,
    PRIMARY KEY (ingestion_job_id, day, category)
);

CREATE INDEX idx_daily_aggregate_day_category
    ON daily_job_category_aggregate (day, category);

CREATE INDEX idx_transaction_job_category
    ON transaction_record (ingestion_job_id, lower(category), category);

CREATE INDEX idx_ingestion_job_created_id
    ON ingestion_job (created_at DESC, id DESC);

INSERT INTO daily_job_category_aggregate (ingestion_job_id, day, category, total_amount, transaction_count)

SELECT ingestion_job_id,
       (occurred_at AT TIME ZONE 'America/Sao_Paulo')::date, category,
       SUM(amount),
       COUNT(*)
FROM transaction_record tr
         JOIN ingestion_job ij ON ij.id = tr.ingestion_job_id
WHERE ij.status IN ('COMPLETED', 'COMPLETED_WITH_ERRORS')
GROUP BY ingestion_job_id, (occurred_at AT TIME ZONE 'America/Sao_Paulo')::date, category
ON CONFLICT (ingestion_job_id, day, category) DO
UPDATE
    SET total_amount = EXCLUDED.total_amount,
    transaction_count = EXCLUDED.transaction_count;

UPDATE ingestion_job
SET total_rows = processed_rows
WHERE status IN ('COMPLETED', 'COMPLETED_WITH_ERRORS', 'FAILED')
  AND total_rows = 0;
