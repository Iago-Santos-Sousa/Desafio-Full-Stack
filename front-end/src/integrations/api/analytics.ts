import type { Aggregate, Summary } from "@/types/api";
import { api } from "@/integrations/api/client";

export const getSummary = async (
  from: string,
  to: string,
): Promise<Summary> => {
  const response = await api.get<Summary>("/api/v1/analytics/summary", {
    params: { from, to },
  });

  return response.data;
};

export const getAggregates = async (
  from: string,
  to: string,
): Promise<Aggregate[]> => {
  const response = await api.get<Aggregate[]>("/api/v1/analytics/monthly", {
    params: { from, to },
  });

  return response.data;
};
