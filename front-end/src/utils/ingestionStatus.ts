import type { JobStatus } from "@/types/api";

export const terminalStatuses: JobStatus[] = [
  "COMPLETED",
  "COMPLETED_WITH_ERRORS",
  "FAILED",
];

export const statusText: Record<JobStatus, string> = {
  RECEIVED: "Recebido",
  QUEUED: "Na fila",
  PROCESSING: "Processando",
  COMPLETED: "Concluído",
  COMPLETED_WITH_ERRORS: "Concluído com erros",
  FAILED: "Falhou",
};
