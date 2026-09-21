import { Alert, Snackbar } from "@mui/material";
import { useCallback, useMemo, useState, type PropsWithChildren } from "react";
import { ToastContext, type ToastOptions } from "@/context/toast-context";

interface ToastItem extends ToastOptions {
  id: number;
}

export function ToastProvider({ children }: PropsWithChildren) {
  const [queue, setQueue] = useState<ToastItem[]>([]);

  const showToast = useCallback(
    (options: ToastOptions) =>
      setQueue((items) => [
        ...items,
        { ...options, id: Date.now() + Math.random() },
      ]),
    [],
  );

  const close = useCallback(() => setQueue((items) => items.slice(1)), []);
  const active = queue[0];
  const value = useMemo(() => ({ showToast }), [showToast]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <Snackbar
        open={Boolean(active)}
        autoHideDuration={active?.durationMs ?? 5000}
        onClose={close}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert
          onClose={close}
          severity={active?.severity ?? "info"}
          variant="filled"
          sx={{ width: "100%" }}
        >
          {active?.message}
        </Alert>
      </Snackbar>
    </ToastContext.Provider>
  );
}
