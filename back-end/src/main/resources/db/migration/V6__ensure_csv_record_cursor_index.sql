CREATE INDEX IF NOT EXISTS idx_csv_record_job_id_id
    ON csv_record(ingestion_job_id, id);

ANALYZE csv_record;
