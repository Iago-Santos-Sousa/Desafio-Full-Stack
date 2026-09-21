import { Button, Stack, Typography } from "@mui/material";
import { CircleHelp, LayoutDashboard } from "lucide-react";
import { Link } from "react-router";

export function NotFoundPage() {
  return (
    <Stack spacing={2} alignItems="flex-start">
      <CircleHelp
        size={48}
        color="var(--mui-palette-primary-main)"
        aria-hidden="true"
      />
      <Typography variant="h4">Página não encontrada</Typography>
      <Button
        component={Link}
        to="/dashboard"
        variant="contained"
        startIcon={<LayoutDashboard size={18} aria-hidden="true" />}
      >
        Ir ao dashboard
      </Button>
    </Stack>
  );
}
