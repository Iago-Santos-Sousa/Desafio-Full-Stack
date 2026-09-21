import { Box, Container } from "@mui/material";
import { Outlet, useNavigation } from "react-router";
import { Navbar } from "@/components/Navbar";
import { GlobalIngestionProgress } from "@/features/ingestions/ActiveIngestionsPanel";
import { LoadingState } from "@/components/StateMessage";

export function AppLayout() {
  const navigation = useNavigation();

  return (
    <Box className="min-h-screen bg-app-background">
      <Navbar />
      <Container maxWidth="xl" className="py-8 md:py-10">
        <GlobalIngestionProgress />
        {navigation.state !== "idle" ? <LoadingState /> : null}
        <Outlet />
      </Container>
    </Box>
  );
}
