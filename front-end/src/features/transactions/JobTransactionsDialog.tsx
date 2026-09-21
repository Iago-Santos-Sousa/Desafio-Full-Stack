import {
  Box,
  Button,
  Dialog,
  DialogContent,
  DialogTitle,
  IconButton,
  Alert,
  LinearProgress,
  Pagination,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { ChevronLeft, ChevronRight, X } from "lucide-react";
import { useRecordsQuery } from "@/hooks/api/useDashboardQueries";
import { useCursorPagination } from "@/hooks/useCursorPagination";
import { getApiErrorMessage } from "@/utils/ingestionMessages";
import type { JobStatus } from "@/types/api";

export function JobTransactionsDialog({
  jobId,
  status,
  open,
  onClose,
}: {
  jobId: string;
  status: JobStatus;
  open: boolean;
  onClose: () => void;
}) {
  const {
    cursor,
    page,
    knownPageCount,
    hasPrevious,
    next,
    previous,
    goToPage,
    reset,
  } = useCursorPagination<number>();

  const records = useRecordsQuery(jobId, cursor, 25, open);
  const columns = records.data?.columns ?? [];
  const loading = records.isFetching || records.isPlaceholderData;
  const skeletonColumnCount = Math.max(columns.length, 3);

  const close = () => {
    reset();
    onClose();
  };

  return (
    <Dialog
      open={open}
      onClose={close}
      fullWidth
      maxWidth="lg"
      aria-labelledby="job-records-title"
    >
      <DialogTitle id="job-records-title">
        Registros do job
        <IconButton
          aria-label="Fechar"
          onClick={close}
          sx={{ position: "absolute", right: 8, top: 8 }}
        >
          <X size={20} aria-hidden="true" />
        </IconButton>
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2}>
          {status === "FAILED" ? (
            <Alert severity="warning">
              Exibindo registros válidos persistidos antes da falha do
              processamento.
            </Alert>
          ) : null}
          {loading ? <LinearProgress /> : null}
          {records.isError ? (
            <Typography color="error">
              {getApiErrorMessage(records.error)}
            </Typography>
          ) : null}
          <Box
            sx={{
              border: 1,
              borderColor: "divider",
              borderRadius: 2,
              overflow: "hidden",
            }}
            aria-busy={loading}
          >
            <Box sx={{ overflowX: "auto" }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>#</TableCell>
                    {loading
                      ? Array.from(
                          { length: skeletonColumnCount },
                          (_, index) => (
                            <TableCell key={`header-skeleton-${index}`}>
                              <Skeleton variant="text" />
                            </TableCell>
                          ),
                        )
                      : columns.map((column) => (
                          <TableCell key={column}>{column}</TableCell>
                        ))}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {loading
                    ? Array.from({ length: 5 }, (_, index) => (
                        <TableRow key={index}>
                          <TableCell>
                            <Skeleton />
                          </TableCell>
                          {Array.from(
                            { length: skeletonColumnCount },
                            (_, columnIndex) => (
                              <TableCell
                                key={`row-${index}-skeleton-${columnIndex}`}
                              >
                                <Skeleton />
                              </TableCell>
                            ),
                          )}
                        </TableRow>
                      ))
                    : records.data?.items.map((row) => (
                        <TableRow key={row.id}>
                          <TableCell>{row.rowNumber}</TableCell>
                          {columns.map((column) => (
                            <TableCell key={column}>
                              {row.values[column] ?? ""}
                            </TableCell>
                          ))}
                        </TableRow>
                      ))}
                </TableBody>
              </Table>
            </Box>
          </Box>
          {!loading &&
          !records.isPending &&
          !records.isError &&
          !records.data?.items.length ? (
            <Typography color="text.secondary">
              {status === "FAILED"
                ? "Nenhum registro válido foi persistido antes da falha."
                : "Nenhum registro encontrado."}
            </Typography>
          ) : null}
          <Stack
            direction={{ xs: "column", sm: "row" }}
            justifyContent="space-between"
            alignItems={{ xs: "stretch", sm: "center" }}
            spacing={1}
          >
            {!loading && records.data?.totalRecords ? (
              <Typography variant="body2" color="text.secondary">
                Página {page} de {records.data.totalPages} ·{" "}
                {records.data.totalRecords.toLocaleString("pt-BR")} registros
              </Typography>
            ) : null}
            {!loading && records.data?.totalPages ? (
              <Pagination
                page={page}
                count={Math.max(
                  1,
                  Math.min(records.data.totalPages, knownPageCount),
                )}
                onChange={(_, selectedPage) => goToPage(selectedPage)}
                disabled={loading || knownPageCount <= 1}
                size="small"
                color="primary"
                aria-label="Paginação de registros"
              />
            ) : null}
            <Stack direction="row" justifyContent="flex-end" spacing={1}>
              <Button
                onClick={previous}
                disabled={loading || !hasPrevious}
                startIcon={<ChevronLeft size={18} />}
              >
                Anterior
              </Button>
              <Button
                variant="outlined"
                onClick={() => next(records.data?.nextCursor)}
                disabled={loading || !records.data?.nextCursor}
                endIcon={<ChevronRight size={18} />}
              >
                Próxima
              </Button>
            </Stack>
          </Stack>
        </Stack>
      </DialogContent>
    </Dialog>
  );
}
