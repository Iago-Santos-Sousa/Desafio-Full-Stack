import { useState } from "react";

type Cursor = string | number;

export function useCursorPagination<TCursor extends Cursor>() {
  const [cursor, setCursor] = useState<TCursor>();
  const [page, setPage] = useState(1);
  const [pageCursors, setPageCursors] = useState<
    Record<number, TCursor | undefined>
  >({ 1: undefined });

  const next = (nextCursor: TCursor | null | undefined) => {
    if (nextCursor == null) return;

    const nextPage = page + 1;
    setPageCursors((previous) => ({ ...previous, [nextPage]: nextCursor }));
    setCursor(nextCursor);
    setPage(nextPage);
  };

  const previous = () => {
    if (page <= 1) return;

    const previousPage = page - 1;
    setCursor(pageCursors[previousPage]);
    setPage(previousPage);
  };

  const goToPage = (targetPage: number) => {
    if (
      !Number.isInteger(targetPage) ||
      targetPage < 1 ||
      !Object.prototype.hasOwnProperty.call(pageCursors, targetPage)
    ) {
      return;
    }

    setCursor(pageCursors[targetPage]);
    setPage(targetPage);
  };

  const reset = () => {
    setCursor(undefined);
    setPage(1);
    setPageCursors({ 1: undefined });
  };

  const knownPageCount = Math.max(...Object.keys(pageCursors).map(Number), 1);

  return {
    cursor,
    page,
    knownPageCount,
    hasPrevious: page > 1,
    next,
    previous,
    goToPage,
    reset,
  };
}
