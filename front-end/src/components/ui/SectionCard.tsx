import { Paper, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

interface SectionCardProps {
  title: string;
  description?: string;
  icon?: ReactNode;
  action?: ReactNode;
  children: ReactNode;
}

export function SectionCard({
  title,
  description,
  icon,
  action,
  children,
}: SectionCardProps) {
  return (
    <Paper component="section" sx={{ p: { xs: 2, sm: 3 } }}>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        alignItems={{ sm: "flex-start" }}
        gap={2}
        mb={3}
      >
        <Stack direction="row" gap={1.5} alignItems="flex-start">
          {icon}
          <Stack gap={0.25}>
            <Typography variant="h6">{title}</Typography>
            {description ? (
              <Typography color="text.secondary" variant="body2">
                {description}
              </Typography>
            ) : null}
          </Stack>
        </Stack>
        {action}
      </Stack>
      {children}
    </Paper>
  );
}
