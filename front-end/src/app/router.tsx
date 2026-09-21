import { createBrowserRouter, redirect } from "react-router";
import { AppLayout } from "@/layouts/AppLayout";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: AppLayout,
    children: [
      { index: true, loader: () => redirect("/dashboard") },
      {
        path: "dashboard",
        lazy: async () => ({
          Component: (await import("@/pages/DashboardPage")).DashboardPage,
        }),
      },
      {
        path: "ingestions",
        lazy: async () => ({
          Component: (await import("@/pages/IngestionJobsPage"))
            .IngestionJobsPage,
        }),
      },
      {
        path: "ingestions/new",
        lazy: async () => ({
          Component: (await import("@/pages/NewIngestionPage"))
            .NewIngestionPage,
        }),
      },
      {
        path: "ingestions/:jobId",
        lazy: async () => ({
          Component: (await import("@/pages/IngestionStatusPage"))
            .IngestionStatusPage,
        }),
      },
      {
        path: "*",
        lazy: async () => ({
          Component: (await import("@/pages/NotFoundPage")).NotFoundPage,
        }),
      },
    ],
  },
]);
