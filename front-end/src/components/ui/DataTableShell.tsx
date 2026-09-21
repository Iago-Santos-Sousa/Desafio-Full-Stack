import { TableContainer } from "@mui/material";
import type { ReactNode } from "react";

export function DataTableShell({ children }: { children: ReactNode }) {
  return <TableContainer sx={{ overflowX: "auto" }}>{children}</TableContainer>;
}
