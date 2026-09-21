export const queryKeys = {
  activeIngestions: ["active-ingestions"] as const,
  ingestionRoot: ["ingestion"] as const,

  ingestionJobs: (cursor?: string) =>
    ["ingestion-jobs", cursor ?? "first"] as const,
  ingestionJobsRoot: ["ingestion-jobs"] as const,

  ingestion: (jobId: string) => ["ingestion", jobId] as const,

  records: (jobId: string, cursor: number | undefined, size: number) =>
    ["records", jobId, cursor, size] as const,
  recordsRoot: ["records"] as const,

  summary: (from: string, to: string) => ["summary", from, to] as const,
  summaryRoot: ["summary"] as const,

  aggregates: (from: string, to: string) => ["aggregates", from, to] as const,
  aggregatesRoot: ["aggregates"] as const,
};
