import { ApiClientError } from "@/integrations/api/client";

const ingestionMessages: Record<string, string> = {
  "Batch processing failed": "Não foi possível processar o arquivo CSV.",
  "Ingestion processing failed": "Não foi possível concluir o processamento.",
  MESSAGE_DELIVERY_TIMEOUT:
    "O processamento não iniciou dentro do tempo esperado.",
};

const apiMessages: Record<string, string> = {
  CSV_HEADER_INVALID: "O cabeçalho do CSV é inválido.",
  INVALID_CSV: "O arquivo CSV é inválido.",
  INVALID_DATE_RANGE: "O período de datas informado é inválido.",
  INVALID_PARAMETER: "Um parâmetro informado é inválido.",
  MISSING_PARAMETER: "Falta um parâmetro obrigatório.",
  NOT_FOUND: "O recurso solicitado não foi encontrado.",
  UPLOAD_TOO_LARGE: "O arquivo excede o limite máximo de 2 GB.",
  INTERNAL_ERROR: "O servidor não conseguiu concluir a operação.",
};

export const getIngestionErrorMessage = (summary?: string): string => {
  if (!summary) return "Não foi possível concluir o processamento do arquivo.";

  return (
    ingestionMessages[summary] ??
    "Não foi possível concluir o processamento do arquivo."
  );
};

export const getApiErrorMessage = (error: unknown): string => {
  if (!(error instanceof ApiClientError)) {
    return "Não foi possível comunicar com a API.";
  }

  if (error.code && apiMessages[error.code]) return apiMessages[error.code];

  if (error.status === 400 || error.status === 422) {
    return "Os dados informados são inválidos.";
  }

  if (error.status === 404) return "O recurso solicitado não foi encontrado.";

  if (error.status === 413) return "O arquivo excede o limite máximo de 2 GB.";

  if (error.status && error.status >= 500) {
    return "O servidor não conseguiu concluir a operação.";
  }

  return "Não foi possível comunicar com a API.";
};
