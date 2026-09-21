import type {
  IngestionJobPage,
  IngestionStatus,
  UploadAccepted,
} from "@/types/api";
import { api } from "@/integrations/api/client";

export const uploadCsv = async (
  file: File,
  onProgress: (value: number) => void,
): Promise<UploadAccepted> => {
  const form = new FormData();

  form.append("file", file);

  const response = await api.post<UploadAccepted>("/api/v1/ingestions", form, {
    timeout: 0,
    onUploadProgress: (event) =>
      onProgress(
        event.total ? Math.round((event.loaded / event.total) * 100) : 0,
      ),
  });

  return response.data;
};

export const getActiveIngestions = async (
  limit = 5,
): Promise<IngestionStatus[]> => {
  const response = await api.get<IngestionStatus[]>(
    "/api/v1/ingestions/active",
    {
      params: { limit },
    },
  );

  return response.data;
};

export const getIngestion = async (id: string): Promise<IngestionStatus> => {
  const response = await api.get<IngestionStatus>(`/api/v1/ingestions/${id}`);
  return response.data;
};

export const getIngestionJobs = async (
  cursor?: string,
  size = 10,
): Promise<IngestionJobPage> => {
  const response = await api.get<IngestionJobPage>("/api/v1/ingestions", {
    params: { cursor, size },
  });

  return response.data;
};
