import { Stack, Typography } from "@mui/material";
import { BarChart3, CheckCircle2, Files, Rows3 } from "lucide-react";
import { lazy, Suspense } from "react";
import { ErrorState, LoadingState } from "@/components/StateMessage";
import { Button } from "@mui/material";
import { useForm } from "react-hook-form";
import { DateRangeFields } from "@/components/DateRangeFields";
import { MetricCard } from "@/components/ui/MetricCard";
import { SectionCard } from "@/components/ui/SectionCard";
import { formatDateRangePtBr } from "@/utils/format";
import {
  useAggregatesQuery,
  useSummaryQuery,
} from "@/hooks/api/useDashboardQueries";
import {
  formatDashboardDate,
  isValidDateRange,
  parseDashboardDate,
  type DashboardDateFormValues,
} from "@/utils/dateRange";
import { getApiErrorMessage } from "@/utils/ingestionMessages";

const MonthlyChart = lazy(() =>
  import("@/features/dashboard/MonthlyChart").then((module) => ({
    default: module.MonthlyChart,
  })),
);

export function AnalyticsPanel({
  from,
  to,
  onRangeChange,
}: {
  from: string;
  to: string;
  onRangeChange: (from: string, to: string) => void;
}) {
  const summary = useSummaryQuery(from, to);
  const aggregates = useAggregatesQuery(from, to);

  const {
    control,
    getValues,
    handleSubmit,
    formState: { isValid },
  } = useForm<DashboardDateFormValues>({
    defaultValues: {
      from: parseDashboardDate(from),
      to: parseDashboardDate(to),
    },
    mode: "onChange",
  });

  const submitRange = (values: DashboardDateFormValues) => {
    const nextFrom = formatDashboardDate(values.from);
    const nextTo = formatDashboardDate(values.to);

    if (isValidDateRange(nextFrom, nextTo)) {
      onRangeChange(nextFrom, nextTo);
    }
  };

  const rows = aggregates.data?.slice(-12) ?? [];

  return (
    <Stack spacing={2.5}>
      {summary.isError && (
        <ErrorState message={getApiErrorMessage(summary.error)} />
      )}
      <Stack
        component="section"
        aria-labelledby="dashboard-filters-title"
        spacing={1.25}
        alignItems="center"
      >
        <Typography
          id="dashboard-filters-title"
          component="h1"
          variant="subtitle1"
          fontSize="2rem"
          fontWeight={700}
        >
          Filtros
        </Typography>
        <Stack
          component="form"
          onSubmit={handleSubmit(submitRange)}
          direction={{ xs: "column", sm: "row" }}
          alignItems={{ xs: "stretch", sm: "center" }}
          justifyContent="center"
          spacing={1.5}
          sx={{ width: "100%", maxWidth: 760 }}
        >
          <DateRangeFields control={control} getValues={getValues} />
          <Button
            type="submit"
            variant="contained"
            disabled={!isValid}
            sx={{
              minWidth: { sm: 112 },
              alignSelf: { xs: "stretch", sm: "auto" },
            }}
          >
            Aplicar
          </Button>
        </Stack>
      </Stack>
      <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
        <MetricCard
          label="Registros"
          value={(summary.data?.recordCount ?? 0).toLocaleString("pt-BR")}
          icon={<Rows3 size={22} />}
          loading={summary.isPending}
        />
        <MetricCard
          label="Linhas válidas"
          value={(summary.data?.validRowCount ?? 0).toLocaleString("pt-BR")}
          icon={<CheckCircle2 size={22} />}
          tone="success"
          loading={summary.isPending}
        />
        <MetricCard
          label="Documentos"
          value={(summary.data?.documentCount ?? 0).toLocaleString("pt-BR")}
          icon={<Files size={22} />}
          tone="info"
          loading={summary.isPending}
        />
      </Stack>
      <SectionCard
        title={`Resumo mensal - Período: ${formatDateRangePtBr(from, to)}`}
        icon={<BarChart3 size={22} aria-hidden="true" />}
      >
        {aggregates.isPending ? (
          <LoadingState />
        ) : aggregates.isError ? (
          <ErrorState message={getApiErrorMessage(aggregates.error)} />
        ) : rows.length ? (
          <Stack sx={{ height: 300 }}>
            <Suspense fallback={<LoadingState />}>
              <MonthlyChart rows={rows} />
            </Suspense>
          </Stack>
        ) : (
          <Typography color="text.secondary">
            Nenhum agregado disponível.
          </Typography>
        )}
      </SectionCard>
    </Stack>
  );
}
