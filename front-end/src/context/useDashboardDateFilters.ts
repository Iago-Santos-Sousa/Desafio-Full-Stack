import { useContext } from "react";
import { DashboardDateFiltersContext } from "@/context/dashboard-date-filters-context";

export function useDashboardDateFilters() {
  const context = useContext(DashboardDateFiltersContext);
  if (!context) {
    throw new Error("DashboardDateFiltersProvider is missing");
  }

  return context;
}
