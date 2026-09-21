import {
  AppBar,
  Box,
  Button,
  Container,
  Drawer,
  IconButton,
  Stack,
  Toolbar,
  Typography,
} from "@mui/material";
import {
  Activity,
  Database,
  LayoutDashboard,
  Menu,
  UploadCloud,
  X,
} from "lucide-react";
import { useState } from "react";
import { NavLink } from "react-router";

const navStyle = {
  justifyContent: "flex-start",
  color: "text.secondary",
  "&.active": {
    color: "primary.main",
    fontWeight: 800,
    bgcolor: "action.hover",
  },
};

const links = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  {
    to: "/ingestions",
    label: "Arquivos processados",
    icon: Database,
    end: true,
  },
  { to: "/ingestions/new", label: "Nova ingestão", icon: UploadCloud },
] as const;

export function Navbar() {
  const [mobileOpen, setMobileOpen] = useState(false);

  const closeMobile = () => setMobileOpen(false);

  const navigation = (mobile = false) => (
    <Stack direction={mobile ? "column" : "row"} spacing={mobile ? 1 : 0.5}>
      {links.map(({ to, label, icon: Icon }) => (
        <Button
          key={to}
          component={NavLink}
          to={to}
          end={to === "/ingestions"}
          onClick={mobile ? closeMobile : undefined}
          startIcon={<Icon size={18} aria-hidden="true" />}
          sx={navStyle}
        >
          {label}
        </Button>
      ))}
    </Stack>
  );

  return (
    <AppBar position="static" elevation={0} component="header">
      <Container maxWidth="xl">
        <Toolbar disableGutters sx={{ minHeight: { xs: 68, sm: 76 } }}>
          <Stack
            direction="row"
            alignItems="center"
            gap={1}
            sx={{ flexGrow: 1 }}
          >
            <Box
              sx={{
                width: 36,
                height: 36,
                display: "grid",
                placeItems: "center",
                borderRadius: 2,
                bgcolor: "primary.main",
                color: "primary.contrastText",
              }}
            >
              <Activity size={21} aria-hidden="true" />
            </Box>
            <Typography variant="h6">DataPulse</Typography>
          </Stack>
          <Box sx={{ display: { xs: "none", md: "block" } }}>
            {navigation()}
          </Box>
          <IconButton
            aria-label={mobileOpen ? "Fechar menu" : "Abrir menu"}
            onClick={() => setMobileOpen((open) => !open)}
            sx={{ display: { xs: "inline-flex", md: "none" } }}
          >
            {mobileOpen ? <X size={22} /> : <Menu size={22} />}
          </IconButton>
        </Toolbar>
      </Container>
      <Drawer
        anchor="right"
        open={mobileOpen}
        onClose={closeMobile}
        sx={{ display: { xs: "block", md: "none" } }}
      >
        <Box
          role="navigation"
          aria-label="Navegação principal"
          sx={{ width: 280, p: 2 }}
        >
          <Stack
            direction="row"
            justifyContent="space-between"
            alignItems="center"
            mb={2}
          >
            <Typography fontWeight={800}>Menu</Typography>
            <IconButton aria-label="Fechar menu" onClick={closeMobile}>
              <X size={20} />
            </IconButton>
          </Stack>
          {navigation(true)}
        </Box>
      </Drawer>
    </AppBar>
  );
}
