import { Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

interface PageHeaderProps {
  title: string;
  description: string;
  icon?: ReactNode;
  eyebrow?: string;
  action?: ReactNode;
}

export function PageHeader({
  title,
  description,
  icon,
  eyebrow,
  action,
}: PageHeaderProps) {
  return (
    <Stack
      component="header"
      direction={{ xs: "column", sm: "row" }}
      justifyContent="space-between"
      alignItems={{ sm: "center" }}
      gap={2}
    >
      <Stack direction="row" gap={1.5} alignItems="flex-start">
        {icon}
        <Stack gap={0.5}>
          {eyebrow ? (
            <Typography variant="overline" color="primary" fontWeight={800}>
              {eyebrow}
            </Typography>
          ) : null}
          <Typography variant="h3" color="text.primary">
            {title}
          </Typography>
          <Typography color="text.secondary">{description}</Typography>
        </Stack>
      </Stack>
      {action}
    </Stack>
  );
}
