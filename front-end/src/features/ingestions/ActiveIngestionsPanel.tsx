import { LinearProgress, Skeleton, Stack, Typography } from "@mui/material";
import { Activity } from "lucide-react";
import type { IngestionStatus } from "@/types/api";
import { SectionCard } from "@/components/ui/SectionCard";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useIngestionLifecycleSync } from "@/hooks/api/useIngestionLifecycleSync";

export function GlobalIngestionProgress() {
  const active = useIngestionLifecycleSync();

  return (
    <ActiveIngestionsPanel
      jobs={active.data ?? []}
      loading={active.isPending}
    />
  );
}

export function ActiveIngestionsPanel({
  jobs,
  loading,
}: {
  jobs: IngestionStatus[];
  loading: boolean;
}) {
  if (!loading && jobs.length === 0) return null;

  return (
    <SectionCard
      title="Processamento em tempo real"
      icon={<Activity size={22} aria-hidden="true" />}
    >
      {loading ? (
        <Stack spacing={1}>
          <Skeleton variant="text" width="60%" />
          <Skeleton variant="rounded" height={8} />
        </Stack>
      ) : (
        <Stack spacing={2}>
          {jobs.map((job) => (
            <Stack key={job.jobId} spacing={0.75}>
              <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                gap={2}
              >
                <Typography variant="body2" noWrap>
                  {job.originalFilename}
                </Typography>
                <StatusBadge status={job.status} />
              </Stack>
              <LinearProgress variant="indeterminate" />
              <Typography
                variant="caption"
                color="text.secondary"
                aria-live="polite"
              >
                {job.status === "QUEUED"
                  ? "Aguardando capacidade de processamento. A fila mantém até 5 processos prontos."
                  : `${job.processedRows.toLocaleString("pt-BR")} linhas lidas · ${job.validRows.toLocaleString("pt-BR")} válidas · ${job.invalidRows.toLocaleString("pt-BR")} inválidas`}
              </Typography>
            </Stack>
          ))}
        </Stack>
      )}
    </SectionCard>
  );
}
