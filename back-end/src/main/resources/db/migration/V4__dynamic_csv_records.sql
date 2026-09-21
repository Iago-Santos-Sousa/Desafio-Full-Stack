DROP TABLE IF EXISTS daily_job_category_aggregate;
DROP TABLE IF EXISTS monthly_category_aggregate;
DROP TABLE IF EXISTS transaction_record;

ALTER TABLE ingestion_job
    ADD COLUMN columns JSONB NOT NULL DEFAULT '[]'::jsonb;

CREATE TABLE csv_record
(
    id               BIGSERIAL PRIMARY KEY,
    ingestion_job_id UUID   NOT NULL REFERENCES ingestion_job (id),
    row_number       BIGINT NOT NULL,
    data             JSONB  NOT NULL,
    CONSTRAINT uq_csv_record_job_row UNIQUE (ingestion_job_id, row_number)
);

CREATE INDEX idx_csv_record_job_id_id
    ON csv_record (ingestion_job_id, id);

CREATE TABLE ingestion_job_metric
(
    ingestion_job_id UUID PRIMARY KEY REFERENCES ingestion_job (id),
    finished_day     DATE   NOT NULL,
    processed_rows   BIGINT NOT NULL,
    valid_rows       BIGINT NOT NULL
);

CREATE INDEX idx_ingestion_job_metric_finished_day
    ON ingestion_job_metric (finished_day, ingestion_job_id);
