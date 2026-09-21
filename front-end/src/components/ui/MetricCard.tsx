import {
  alpha,
  Box,
  Paper,
  Skeleton,
  Stack,
  Typography,
  useTheme,
} from "@mui/material";
import type { ReactNode } from "react";
import type { MetricTone } from "@/app/designTokens";

interface MetricCardProps {
  label: string;
  value: ReactNode;
  icon: ReactNode;
  tone?: MetricTone;
  loading?: boolean;
}

export function MetricCard({
  label,
  value,
  icon,
  tone = "primary",
  loading = false,
}: MetricCardProps) {
  const theme = useTheme();
  const color =
    tone === "neutral"
      ? theme.palette.text.secondary
      : theme.palette[tone].main;

  return (
    <Paper sx={{ p: 2.5, flex: 1, minWidth: 0 }}>
      <Stack direction="row" justifyContent="space-between" gap={2}>
        <Stack gap={0.5} minWidth={0}>
          <Typography variant="body2" color="text.secondary" noWrap>
            {label}
          </Typography>
          {loading ? (
            <Skeleton width={90} height={38} />
          ) : (
            <Typography variant="h5">{value}</Typography>
          )}
        </Stack>
        <Box
          sx={{
            width: 44,
            height: 44,
            display: "grid",
            placeItems: "center",
            flexShrink: 0,
            borderRadius: 2.5,
            color,
            bgcolor: alpha(color, 0.1),
          }}
        >
          {icon}
        </Box>
      </Stack>
    </Paper>
  );
}
