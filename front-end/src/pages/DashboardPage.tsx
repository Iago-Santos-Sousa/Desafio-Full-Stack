import { Stack } from "@mui/material";
import { Gauge } from "lucide-react";
import { useEffect, useMemo } from "react";
import { useSearchParams } from "react-router";
import { PageHeader } from "@/components/PageHeader";
import { useDashboardDateFilters } from "@/context/useDashboardDateFilters";
import { AnalyticsPanel } from "@/features/dashboard/AnalyticsPanel";
import { isValidDateRange } from "@/utils/dateRange";

export function DashboardPage() {
  const [params, setParams] = useSearchParams();
  const { filters, setFilters } = useDashboardDateFilters();
  const urlFrom = params.get("from");
  const urlTo = params.get("to");

  const urlFilters = useMemo(
    () =>
      urlFrom && urlTo && isValidDateRange(urlFrom, urlTo)
        ? { from: urlFrom, to: urlTo }
        : null,
    [urlFrom, urlTo],
  );

  const effectiveFilters = urlFilters ?? filters;

  useEffect(() => {
    if (
      filters.from !== effectiveFilters.from ||
      filters.to !== effectiveFilters.to
    )
      setFilters(effectiveFilters);

    if (urlFrom !== effectiveFilters.from || urlTo !== effectiveFilters.to) {
      const nextParams = new URLSearchParams(params);
      nextParams.set("from", effectiveFilters.from);
      nextParams.set("to", effectiveFilters.to);
      setParams(nextParams, { replace: true });
    }
  }, [
    effectiveFilters,
    filters.from,
    filters.to,
    params,
    setFilters,
    setParams,
    urlFrom,
    urlTo,
  ]);

  const updateRange = (nextFrom: string, nextTo: string) => {
    if (!isValidDateRange(nextFrom, nextTo)) return;
    setFilters({ from: nextFrom, to: nextTo });
    setParams({ from: nextFrom, to: nextTo });
  };

  return (
    <Stack spacing={3} marginTop={2}>
      <PageHeader
        title="DataPulse"
        description="Processador de arquivos CSV em larga escala, sem travar sua tela."
        icon={
          <Gauge
            size={28}
            color="var(--mui-palette-primary-main)"
            aria-hidden="true"
          />
        }
      />
      <AnalyticsPanel
        key={`${effectiveFilters.from}-${effectiveFilters.to}`}
        from={effectiveFilters.from}
        to={effectiveFilters.to}
        onRangeChange={updateRange}
      />
    </Stack>
  );
}
