ALTER TABLE ingestion_job
    ADD COLUMN csv_delimiter VARCHAR(1) NOT NULL DEFAULT ',',
    ADD COLUMN csv_encoding VARCHAR(40) NOT NULL DEFAULT 'UTF-8';
