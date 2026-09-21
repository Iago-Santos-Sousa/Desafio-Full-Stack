import type { RecordPage } from "@/types/api";
import { api } from "@/integrations/api/client";

export const getRecords = async (
  jobId: string,
  cursor?: number,
  size = 25,
  signal?: AbortSignal,
): Promise<RecordPage> => {
  const response = await api.get<RecordPage>(
    `/api/v1/ingestions/${jobId}/records`,
    { params: { cursor, size }, signal },
  );

  return response.data;
};
