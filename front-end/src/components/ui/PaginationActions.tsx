import { Button, Stack } from "@mui/material";
import { ChevronLeft, ChevronRight } from "lucide-react";

interface PaginationActionsProps {
  hasPrevious: boolean;
  hasNext: boolean;
  onPrevious: () => void;
  onNext: () => void;
}

export function PaginationActions({
  hasPrevious,
  hasNext,
  onPrevious,
  onNext,
}: PaginationActionsProps) {
  return (
    <Stack direction="row" gap={1} justifyContent="flex-end" marginTop={2}>
      <Button
        variant="outlined"
        size="small"
        disabled={!hasPrevious}
        onClick={onPrevious}
        startIcon={<ChevronLeft size={18} />}
      >
        Anterior
      </Button>
      <Button
        variant="outlined"
        size="small"
        disabled={!hasNext}
        onClick={onNext}
        endIcon={<ChevronRight size={18} />}
      >
        Próxima
      </Button>
    </Stack>
  );
}
