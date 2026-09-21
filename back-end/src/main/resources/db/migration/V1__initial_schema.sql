CREATE TABLE ingestion_job
(
    id                UUID PRIMARY KEY,
    original_filename VARCHAR(255)  NOT NULL,
    stored_path       VARCHAR(1024) NOT NULL,
    file_size_bytes   BIGINT        NOT NULL,
    status            VARCHAR(32)   NOT NULL,
    total_rows        BIGINT        NOT NULL DEFAULT 0,
    processed_rows    BIGINT        NOT NULL DEFAULT 0,
    valid_rows        BIGINT        NOT NULL DEFAULT 0,
    invalid_rows      BIGINT        NOT NULL DEFAULT 0,
    error_summary     VARCHAR(1000),
    created_at        TIMESTAMPTZ   NOT NULL,
    queued_at         TIMESTAMPTZ,
    started_at        TIMESTAMPTZ,
    finished_at       TIMESTAMPTZ,
    updated_at        TIMESTAMPTZ   NOT NULL
);

CREATE TABLE transaction_record
(
    id               BIGSERIAL PRIMARY KEY,
    ingestion_job_id UUID           NOT NULL REFERENCES ingestion_job (id),
    occurred_at      TIMESTAMPTZ    NOT NULL,
    category         VARCHAR(120)   NOT NULL,
    amount           NUMERIC(19, 4) NOT NULL,
    description      VARCHAR(500)
);

CREATE TABLE monthly_category_aggregate
(
    month             DATE           NOT NULL,
    category          VARCHAR(120)   NOT NULL,
    total_amount      NUMERIC(24, 4) NOT NULL,
    transaction_count BIGINT         NOT NULL,
    PRIMARY KEY (month, category)
);

CREATE INDEX idx_transaction_job_id ON transaction_record (ingestion_job_id, id);
CREATE INDEX idx_transaction_occurred_id ON transaction_record (occurred_at, id);
CREATE INDEX idx_transaction_category_date ON transaction_record (category, occurred_at, id);
