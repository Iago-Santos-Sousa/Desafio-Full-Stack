import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { getAggregates, getSummary } from "@/integrations/api/analytics";
import { getRecords } from "@/integrations/api/records";
import { queryKeys } from "@/hooks/api/queryKeys";

export const useSummaryQuery = (from: string, to: string) => {
  return useQuery({
    queryKey: queryKeys.summary(from, to),
    queryFn: () => getSummary(from, to),
  });
};

export const useAggregatesQuery = (from: string, to: string) => {
  return useQuery({
    queryKey: queryKeys.aggregates(from, to),
    queryFn: () => getAggregates(from, to),
  });
};

export const useRecordsQuery = (
  jobId: string,
  cursor: number | undefined,
  size = 25,
  enabled = true,
) => {
  return useQuery({
    queryKey: queryKeys.records(jobId, cursor, size),
    queryFn: ({ signal }) => getRecords(jobId, cursor, size, signal),
    placeholderData: keepPreviousData,
    enabled,
  });
};
