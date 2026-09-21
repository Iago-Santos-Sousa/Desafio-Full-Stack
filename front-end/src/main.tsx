import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "@/index.css";
import { RouterProvider } from "react-router";
import { GlobalStyles } from "@mui/material";
import { StyledEngineProvider } from "@mui/material/styles";
import { AppProviders } from "@/app/providers";
import { router } from "@/app/router";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <StyledEngineProvider enableCssLayer>
      <GlobalStyles styles="@layer theme, base, mui, components, utilities;" />
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>
    </StyledEngineProvider>
  </StrictMode>,
);
