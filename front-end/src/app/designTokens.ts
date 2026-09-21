export const designTokens = {
  colors: {
    primary: "#2563EB",
    primaryDark: "#1D4ED8",
    secondary: "#047857",
    secondaryDark: "#065F46",
    accent: "#0E7490",
    background: "#F8FAFC",
    surface: "#FFFFFF",
    surfaceMuted: "#F1F5F9",
    textPrimary: "#0F172A",
    textSecondary: "#475569",
    border: "#DCE6F0",
    success: "#15803D",
    warning: "#B45309",
    error: "#B91C1C",
    info: "#0369A1",
  },
  chart: ["#2563EB", "#047857", "#0E7490", "#7C3AED", "#B45309", "#BE185D"],
  typography: {
    sans: 'Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
  },
} as const;

export type MetricTone = "primary" | "success" | "info" | "warning" | "neutral";
