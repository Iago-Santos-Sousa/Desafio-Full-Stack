import { format, isValid, parse } from "date-fns";

export const formatCurrency = (value: number): string => {
  return `R$ ${value.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`;
};

export const formatDateTime = (value: string): string => {
  return new Date(value).toLocaleString("pt-BR");
};

export const formatBytes = (value: number): string => {
  if (value < 1024) return `${value} B`;
  const units = ["KB", "MB", "GB", "TB"];
  let size = value;
  let index = -1;

  do {
    size /= 1024;
    index += 1;
  } while (size >= 1024 && index < units.length - 1);

  return `${size.toLocaleString("pt-BR", { maximumFractionDigits: 1 })} ${units[index]}`;
};

const parseApiDate = (value: string): Date | null => {
  const datePart = value.slice(0, 10);
  const parsed = parse(datePart, "yyyy-MM-dd", new Date());

  return isValid(parsed) && format(parsed, "yyyy-MM-dd") === datePart
    ? parsed
    : null;
};

export const formatCalendarDatePtBr = (value: string): string => {
  const parsed = parseApiDate(value);
  return parsed ? format(parsed, "dd/MM/yyyy") : value;
};

export const formatMonthPtBr = (value: string): string => {
  const parsed = parseApiDate(value);
  return parsed ? format(parsed, "MM/yyyy") : value.slice(0, 7);
};

export const formatDateRangePtBr = (from: string, to: string): string => {
  return `${formatCalendarDatePtBr(from)} a ${formatCalendarDatePtBr(to)}`;
};
