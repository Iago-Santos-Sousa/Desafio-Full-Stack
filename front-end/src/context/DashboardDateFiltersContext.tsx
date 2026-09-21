import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type PropsWithChildren,
} from "react";
import {
  defaultDashboardDateRange,
  isValidDateRange,
  type DashboardDateRange,
} from "@/utils/dateRange";
import { DashboardDateFiltersContext } from "@/context/dashboard-date-filters-context";

const STORAGE_KEY = "datapulse.dashboard.date-range.v1";

const readStoredFilters = (): DashboardDateRange | null => {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (!raw) return null;

    const parsed: unknown = JSON.parse(raw);
    if (
      typeof parsed !== "object" ||
      parsed === null ||
      !("from" in parsed) ||
      !("to" in parsed) ||
      typeof parsed.from !== "string" ||
      typeof parsed.to !== "string" ||
      !isValidDateRange(parsed.from, parsed.to)
    ) {
      return null;
    }

    return { from: parsed.from, to: parsed.to };
  } catch {
    return null;
  }
};

export function DashboardDateFiltersProvider({ children }: PropsWithChildren) {
  const [filters, setFiltersState] = useState<DashboardDateRange>(
    () => readStoredFilters() ?? defaultDashboardDateRange(),
  );

  const setFilters = useCallback((next: DashboardDateRange) => {
    if (isValidDateRange(next.from, next.to)) {
      setFiltersState(next);
    }
  }, []);

  const resetFilters = useCallback(() => {
    setFiltersState(defaultDashboardDateRange());
  }, []);

  useEffect(() => {
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(filters));
    } catch {
      // O armazenamento pode não estar disponível em contextos de navegador restritos.
    }
  }, [filters]);

  const value = useMemo(
    () => ({ filters, setFilters, resetFilters }),
    [filters, resetFilters, setFilters],
  );

  return (
    <DashboardDateFiltersContext.Provider value={value}>
      {children}
    </DashboardDateFiltersContext.Provider>
  );
}
