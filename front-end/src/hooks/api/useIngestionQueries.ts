import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import {
  getActiveIngestions,
  getIngestionJobs,
  getIngestion,
  uploadCsv,
} from "@/integrations/api/ingestions";
import { queryKeys } from "@/hooks/api/queryKeys";

export const useUploadCsvMutation = (onProgress: (value: number) => void) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (file: File) => uploadCsv(file, onProgress),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: queryKeys.activeIngestions,
      });

      void queryClient.invalidateQueries({
        queryKey: queryKeys.ingestionJobsRoot,
      });
    },
  });
};

export const useIngestionQuery = (jobId: string | undefined) => {
  return useQuery({
    queryKey: queryKeys.ingestion(jobId ?? ""),
    queryFn: () => getIngestion(jobId as string),
    enabled: Boolean(jobId),
    refetchInterval: (query) => {
      const status = query.state.data?.status;

      return status === "COMPLETED" ||
        status === "COMPLETED_WITH_ERRORS" ||
        status === "FAILED"
        ? false
        : status === "PROCESSING"
          ? 2000
          : 1000;
    },
  });
};

export const useActiveIngestionsQuery = () => {
  return useQuery({
    queryKey: queryKeys.activeIngestions,
    queryFn: () => getActiveIngestions(),
    refetchInterval: (query) =>
      query.state.data && query.state.data.length > 0 ? 2000 : false,
  });
};

export const useIngestionJobsQuery = (cursor?: string) => {
  return useQuery({
    queryKey: queryKeys.ingestionJobs(cursor),
    queryFn: () => getIngestionJobs(cursor),
    placeholderData: keepPreviousData,
    refetchInterval: (query) => {
      const active = query.state.data?.items.some((job) =>
        ["RECEIVED", "QUEUED", "PROCESSING"].includes(job.status),
      );

      return active ? 2000 : false;
    },
  });
};
