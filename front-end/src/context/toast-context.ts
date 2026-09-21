import { createContext } from "react";
import type { AlertColor } from "@mui/material";

export interface ToastOptions {
  message: string;
  severity?: AlertColor;
  durationMs?: number;
}

export interface ToastApi {
  showToast: (options: ToastOptions) => void;
}

export const ToastContext = createContext<ToastApi | undefined>(undefined);
