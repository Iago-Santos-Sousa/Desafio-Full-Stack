import {
  Box,
  Button,
  LinearProgress,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from "@mui/material";
import { Database, Eye } from "lucide-react";
import { Link } from "react-router";
import type { IngestionJobListItem } from "@/types/api";
import { formatBytes, formatDateTime } from "@/utils/format";
import { DataTableShell } from "@/components/ui/DataTableShell";
import { PaginationActions } from "@/components/ui/PaginationActions";
import { SectionCard } from "@/components/ui/SectionCard";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { getApiErrorMessage } from "@/utils/ingestionMessages";

const activeStatuses = new Set(["RECEIVED", "QUEUED", "PROCESSING"]);

export function JobsPanel({
  jobs,
  loading,
  hasNext,
  hasPrevious,
  onNext,
  onPrevious,
  error,
}: {
  jobs: IngestionJobListItem[];
  loading: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
  onNext: () => void;
  onPrevious: () => void;
  error: unknown;
}) {
  return (
    <SectionCard
      title="Arquivos processados"
      icon={<Database size={22} aria-hidden="true" />}
    >
      {error ? (
        <Typography color="error" sx={{ py: 2 }}>
          {getApiErrorMessage(error)}
        </Typography>
      ) : null}
      <DataTableShell>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Arquivo</TableCell>
              <TableCell>Tamanho</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Criado em</TableCell>
              <TableCell align="right">Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading
              ? Array.from({ length: 5 }, (_, index) => (
                  <TableRow key={`job-skeleton-${index}`}>
                    {Array.from({ length: 5 }, (_, cell) => (
                      <TableCell key={cell}>
                        <Skeleton />
                      </TableCell>
                    ))}
                  </TableRow>
                ))
              : jobs.map((job) => {
                  const active = activeStatuses.has(job.status);
                  return (
                    <TableRow key={job.jobId} hover>
                      <TableCell>{job.originalFilename}</TableCell>
                      <TableCell>{formatBytes(job.fileSizeBytes)}</TableCell>
                      <TableCell>
                        <Stack spacing={0.75} minWidth={140}>
                          <StatusBadge status={job.status} />
                          {active ? (
                            <LinearProgress aria-label="Job em processamento" />
                          ) : null}
                        </Stack>
                      </TableCell>
                      <TableCell>{formatDateTime(job.createdAt)}</TableCell>
                      <TableCell align="right">
                        <Tooltip title="Ver detalhes" arrow>
                          <Button
                            component={Link}
                            to={`/ingestions/${job.jobId}`}
                            size="small"
                            variant="outlined"
                            aria-label="Ver detalhes"
                            startIcon={<Eye size={17} aria-hidden="true" />}
                            sx={{
                              "@media (max-width: 849px)": {
                                minWidth: 44,
                                width: 44,
                                px: 0.5,
                                "& .MuiButton-startIcon": { m: 0 },
                              },
                            }}
                          >
                            <Box
                              component="span"
                              sx={{
                                "@media (max-width: 849px)": {
                                  display: "none",
                                },
                              }}
                            >
                              Ver detalhes
                            </Box>
                          </Button>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  );
                })}
          </TableBody>
        </Table>
      </DataTableShell>
      {!loading && jobs.length === 0 ? (
        <Typography color="text.secondary" sx={{ py: 3 }}>
          Nenhum job encontrado.
        </Typography>
      ) : null}
      <PaginationActions
        hasPrevious={hasPrevious}
        hasNext={hasNext}
        onPrevious={onPrevious}
        onNext={onNext}
      />
    </SectionCard>
  );
}
