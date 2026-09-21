CREATE TABLE ingestion_event_outbox
(
    id              UUID PRIMARY KEY,
    job_id          UUID        NOT NULL UNIQUE REFERENCES ingestion_job (id),
    event_type      VARCHAR(64) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    attempts        INTEGER     NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    last_error      VARCHAR(1000),
    created_at      TIMESTAMPTZ NOT NULL,
    published_at    TIMESTAMPTZ
);

CREATE INDEX idx_ingestion_outbox_pending
    ON ingestion_event_outbox (status, next_attempt_at, created_at);

INSERT INTO ingestion_event_outbox (id, job_id, event_type, status, attempts, next_attempt_at, created_at)
SELECT id, id, 'INGESTION_ACCEPTED', 'PENDING', 0, CURRENT_TIMESTAMP, created_at
FROM ingestion_job
WHERE status IN ('RECEIVED', 'QUEUED');
