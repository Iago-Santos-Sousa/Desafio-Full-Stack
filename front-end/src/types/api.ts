export type JobStatus =
  | "RECEIVED"
  | "QUEUED"
  | "PROCESSING"
  | "COMPLETED"
  | "COMPLETED_WITH_ERRORS"
  | "FAILED";

export interface IngestionStatus {
  jobId: string;
  originalFilename: string;
  status: JobStatus;
  totalRows: number;
  processedRows: number;
  validRows: number;
  invalidRows: number;
  columns: string[];
  fileSizeBytes: number;
  createdAt: string;
  startedAt?: string;
  finishedAt?: string;
  updatedAt: string;
  errorSummary?: string;
}

export interface IngestionJobListItem {
  jobId: string;
  originalFilename: string;
  fileSizeBytes: number;
  status: JobStatus;
  createdAt: string;
  updatedAt: string;
}

export interface IngestionJobPage {
  items: IngestionJobListItem[];
  nextCursor: string | null;
}

export interface UploadAccepted {
  jobId: string;
  status: JobStatus;
  statusUrl: string;
}

export interface ApiProblem {
  status?: number;
  code?: string;
  title?: string;
  detail?: string;
  timestamp?: string;
  traceId?: string;
}

export interface Summary {
  recordCount: number;
  validRowCount: number;
  documentCount: number;
}

export interface Aggregate {
  month: string;
  recordCount: number;
}

export interface CsvRecord {
  id: number;
  rowNumber: number;
  values: Record<string, string>;
}

export interface RecordPage {
  columns: string[];
  items: CsvRecord[];
  nextCursor: number | null;
  totalRecords: number;
  totalPages: number;
}
