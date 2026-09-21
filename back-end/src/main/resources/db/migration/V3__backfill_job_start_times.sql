UPDATE ingestion_job
SET started_at = COALESCE(queued_at, created_at)
WHERE status IN ('COMPLETED', 'COMPLETED_WITH_ERRORS', 'FAILED')
  AND started_at IS NULL;
