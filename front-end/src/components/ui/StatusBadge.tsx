import { Chip } from "@mui/material";
import {
  AlertTriangle,
  CheckCircle2,
  CircleDashed,
  LoaderCircle,
  XCircle,
} from "lucide-react";
import type { JobStatus } from "@/types/api";
import { statusText } from "@/utils/ingestionStatus";

const statusConfig: Record<
  JobStatus,
  {
    color: "success" | "warning" | "error" | "info";
    icon: typeof CheckCircle2;
    spinning?: boolean;
  }
> = {
  RECEIVED: { color: "info", icon: CircleDashed },
  QUEUED: { color: "info", icon: CircleDashed },
  PROCESSING: { color: "info", icon: LoaderCircle, spinning: true },
  COMPLETED: { color: "success", icon: CheckCircle2 },
  COMPLETED_WITH_ERRORS: { color: "warning", icon: AlertTriangle },
  FAILED: { color: "error", icon: XCircle },
};

export function StatusBadge({ status }: { status: JobStatus }) {
  const config = statusConfig[status];
  const Icon = config.icon;

  return (
    <Chip
      size="small"
      color={config.color}
      variant="outlined"
      icon={
        <Icon
          size={16}
          className={config.spinning ? "motion-safe:animate-spin" : undefined}
          aria-hidden="true"
        />
      }
      label={statusText[status]}
    />
  );
}
