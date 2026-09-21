import { createTheme } from "@mui/material/styles";
import { designTokens } from "@/app/designTokens";

export const theme = createTheme({
  cssVariables: true,
  modularCssLayers: "@layer theme, base, mui, components, utilities;",
  palette: {
    mode: "light",
    primary: {
      main: designTokens.colors.primary,
      dark: designTokens.colors.primaryDark,
      contrastText: designTokens.colors.surface,
    },
    secondary: {
      main: designTokens.colors.secondary,
      dark: designTokens.colors.secondaryDark,
      contrastText: designTokens.colors.surface,
    },
    success: { main: designTokens.colors.success },
    warning: { main: designTokens.colors.warning },
    error: { main: designTokens.colors.error },
    info: { main: designTokens.colors.info },
    background: {
      default: designTokens.colors.background,
      paper: designTokens.colors.surface,
    },
    text: {
      primary: designTokens.colors.textPrimary,
      secondary: designTokens.colors.textSecondary,
    },
    divider: designTokens.colors.border,
  },
  typography: {
    fontFamily: designTokens.typography.sans,
    h3: { fontWeight: 800, letterSpacing: "-0.025em" },
    h5: { fontWeight: 800, letterSpacing: "-0.02em" },
    h6: { fontWeight: 800 },
    body1: { lineHeight: 1.55 },
    button: { fontWeight: 700, textTransform: "none" },
  },
  shape: { borderRadius: 12 },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        "*, *::before, *::after": { boxSizing: "border-box" },
        body: { margin: 0, minWidth: 320 },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { minHeight: 42, borderRadius: 12, paddingInline: 18 },
      },
    },
    MuiIconButton: {
      styleOverrides: { root: { minWidth: 44, minHeight: 44 } },
    },
    MuiPaper: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          backgroundImage: "none",
          border: `1px solid ${designTokens.colors.border}`,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: designTokens.colors.surface,
          color: designTokens.colors.textPrimary,
          borderBottom: `1px solid ${designTokens.colors.border}`,
        },
      },
    },
    MuiChip: {
      styleOverrides: { root: { fontWeight: 700, borderRadius: 999 } },
    },
    MuiTableCell: {
      styleOverrides: {
        head: { fontWeight: 800, color: designTokens.colors.textSecondary },
      },
    },
    MuiDialog: { styleOverrides: { paper: { borderRadius: 16 } } },
    MuiTooltip: { styleOverrides: { tooltip: { fontSize: "0.8rem" } } },
    MuiLinearProgress: {
      styleOverrides: { root: { borderRadius: 999, height: 8 } },
    },
  },
});
