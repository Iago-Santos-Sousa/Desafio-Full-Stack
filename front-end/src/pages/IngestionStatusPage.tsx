import { Button, Stack } from "@mui/material";
import { Activity, ArrowLeft } from "lucide-react";
import { Link, useParams } from "react-router";
import { PageHeader } from "@/components/PageHeader";
import { JobStatusPanel } from "@/features/ingestions/JobStatusPanel";
import { ErrorState, LoadingState } from "@/components/StateMessage";
import { useIngestionQuery } from "@/hooks/api/useIngestionQueries";
import { getApiErrorMessage } from "@/utils/ingestionMessages";

export function IngestionStatusPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const status = useIngestionQuery(jobId);

  return (
    <Stack spacing={3} marginTop={2}>
      <PageHeader
        title="Acompanhamento"
        description={`Job ${jobId ?? ""}`}
        icon={
          <Activity
            size={28}
            color="var(--mui-palette-primary-main)"
            aria-hidden="true"
          />
        }
      />
      {status.isLoading ? <LoadingState /> : null}
      {status.isError ? (
        <ErrorState message={getApiErrorMessage(status.error)} />
      ) : null}
      {status.data ? (
        <JobStatusPanel status={status.data} fetching={status.isFetching} />
      ) : null}
      <Button
        component={Link}
        to="/dashboard"
        variant="outlined"
        startIcon={<ArrowLeft size={18} aria-hidden="true" />}
      >
        Voltar ao dashboard
      </Button>
    </Stack>
  );
}
