import { Bar } from "react-chartjs-2";
import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  type ChartOptions,
  Legend,
  LinearScale,
  Tooltip,
} from "chart.js";
import { designTokens } from "@/app/designTokens";
import { formatMonthPtBr } from "@/utils/format";
import type { Aggregate } from "@/types/api";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

export function MonthlyChart({ rows }: { rows: Aggregate[] }) {
  const chart = {
    labels: rows.map((row) => formatMonthPtBr(row.month)),
    datasets: [
      {
        label: "Registros processados",
        data: rows.map((row) => row.recordCount),
        backgroundColor: designTokens.chart[0],
        borderRadius: 8,
      },
    ],
  };

  const chartOptions: ChartOptions<"bar"> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          title: (items) => items[0]?.label ?? "",
        },
      },
    },
  };

  return <Bar data={chart} options={chartOptions} />;
}
