import { useEffect, useRef } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useToast } from "@/context/useToast";
import { getIngestion } from "@/integrations/api/ingestions";
import type { JobStatus } from "@/types/api";
import { getIngestionErrorMessage } from "@/utils/ingestionMessages";
import { terminalStatuses } from "@/utils/ingestionStatus";
import { queryKeys } from "@/hooks/api/queryKeys";
import { useActiveIngestionsQuery } from "@/hooks/api/useIngestionQueries";

const invalidateIngestionData = (
  queryClient: ReturnType<typeof useQueryClient>,
) => {
  void Promise.all([
    queryClient.invalidateQueries({ queryKey: queryKeys.activeIngestions }),
    queryClient.invalidateQueries({ queryKey: queryKeys.ingestionJobsRoot }),
    queryClient.invalidateQueries({ queryKey: queryKeys.summaryRoot }),
    queryClient.invalidateQueries({ queryKey: queryKeys.aggregatesRoot }),
    queryClient.invalidateQueries({ queryKey: queryKeys.recordsRoot }),
  ]);
};

export const useIngestionLifecycleSync = () => {
  const active = useActiveIngestionsQuery();
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const previousActive = useRef<Map<string, JobStatus>>(new Map());
  const notified = useRef<Set<string>>(new Set());

  useEffect(() => {
    if (!active.data) return;

    const currentActive = new Map(
      active.data.map((job) => [job.jobId, job.status] as const),
    );

    const completedIds = [...previousActive.current.keys()].filter(
      (jobId) => !currentActive.has(jobId),
    );

    previousActive.current = currentActive;

    if (completedIds.length === 0) return;

    let cancelled = false;

    const syncCompletedJobs = async () => {
      await Promise.all(
        completedIds.map(async (jobId) => {
          try {
            const status = await queryClient.fetchQuery({
              queryKey: queryKeys.ingestion(jobId),
              queryFn: () => getIngestion(jobId),
              staleTime: 0,
            });

            queryClient.setQueryData(queryKeys.ingestion(jobId), status);

            if (!terminalStatuses.includes(status.status)) return;

            const notificationKey = `${jobId}:${status.status}`;
            if (notified.current.has(notificationKey) || cancelled) return;

            notified.current.add(notificationKey);

            showToast({
              message:
                status.status === "FAILED"
                  ? getIngestionErrorMessage(status.errorSummary)
                  : status.status === "COMPLETED_WITH_ERRORS"
                    ? "Ingestão concluída com linhas inválidas."
                    : "Ingestão concluída com sucesso.",
              severity:
                status.status === "FAILED"
                  ? "error"
                  : status.status === "COMPLETED_WITH_ERRORS"
                    ? "warning"
                    : "success",
              durationMs: status.status === "FAILED" ? 8000 : 5000,
            });
          } catch {
            // A próxima invalidação/foco da janela poderá recuperar o status.
          }
        }),
      );

      if (!cancelled) invalidateIngestionData(queryClient);
    };

    void syncCompletedJobs();

    return () => {
      cancelled = true;
    };
  }, [active.data, queryClient, showToast]);

  return active;
};
