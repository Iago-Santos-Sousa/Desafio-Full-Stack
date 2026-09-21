import { Stack } from "@mui/material";
import { UploadCloud } from "lucide-react";
import { PageHeader } from "@/components/PageHeader";
import { UploadPanel } from "@/features/ingestions/UploadPanel";

export function NewIngestionPage() {
  return (
    <Stack spacing={3} marginTop={2}>
      <PageHeader
        title="Nova ingestão"
        description="Envie CSV para processamento assíncrono."
        icon={
          <UploadCloud
            size={28}
            color="var(--mui-palette-primary-main)"
            aria-hidden="true"
          />
        }
      />
      <UploadPanel />
    </Stack>
  );
}
