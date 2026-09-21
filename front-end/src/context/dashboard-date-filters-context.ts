import { createContext } from "react";
import type { DashboardDateRange } from "@/utils/dateRange";

export interface DashboardDateFiltersContextValue {
  filters: DashboardDateRange;
  setFilters: (filters: DashboardDateRange) => void;
  resetFilters: () => void;
}

export const DashboardDateFiltersContext = createContext<
  DashboardDateFiltersContextValue | undefined
>(undefined);
