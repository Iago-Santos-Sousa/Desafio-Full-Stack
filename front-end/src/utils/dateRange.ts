import {
  format,
  isBefore,
  isAfter,
  isValid,
  parse,
  startOfMonth,
} from "date-fns";

export interface DashboardDateFormValues {
  from: Date | null;
  to: Date | null;
}

export interface DashboardDateRange {
  from: string;
  to: string;
}

const BUSINESS_TIME_ZONE = "America/Sao_Paulo";
const API_DATE_FORMAT = "yyyy-MM-dd";
export const MIN_DASHBOARD_YEAR = 1900;

export const currentBusinessDate = (): string => {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: BUSINESS_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date());

  const values = Object.fromEntries(
    parts.map((part) => [part.type, part.value]),
  );

  return `${values.year}-${values.month}-${values.day}`;
};

export const defaultDashboardDateRange = (): DashboardDateRange => {
  const to = currentBusinessDate();
  const parsedTo = parse(to, API_DATE_FORMAT, new Date());

  return {
    from: format(startOfMonth(parsedTo), API_DATE_FORMAT),
    to: format(parsedTo, API_DATE_FORMAT),
  };
};

export const businessTodayDate = (): Date => {
  return parse(currentBusinessDate(), API_DATE_FORMAT, new Date());
};

export const minDashboardDate = (): Date => {
  return new Date(MIN_DASHBOARD_YEAR, 0, 1);
};

export const validateDashboardDate = (value: Date | null): true | string => {
  if (!value || !isValid(value)) return "Informe uma data válida.";

  if (value.getFullYear() < MIN_DASHBOARD_YEAR) {
    return `O ano deve ser igual ou posterior a ${MIN_DASHBOARD_YEAR}.`;
  }

  if (isAfter(value, businessTodayDate())) {
    return "A data não pode ser futura.";
  }

  return true;
};

export const isValidDateRange = (from: string, to: string): boolean => {
  if (!from || !to) return false;

  const parsedFrom = parse(from, API_DATE_FORMAT, new Date());
  const parsedTo = parse(to, API_DATE_FORMAT, new Date());

  return (
    isSupportedDashboardDate(from) &&
    isSupportedDashboardDate(to) &&
    !isAfter(parsedFrom, parsedTo)
  );
};

export const parseDashboardDate = (value: string): Date | null => {
  if (!value) return null;

  const parsed = parse(value, API_DATE_FORMAT, new Date());
  return isValid(parsed) ? parsed : null;
};

export const formatDashboardDate = (value: Date | null): string => {
  return value && isValid(value) ? format(value, API_DATE_FORMAT) : "";
};

export const isSupportedDashboardDate = (value: string): boolean => {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;

  const parsed = parse(value, API_DATE_FORMAT, new Date());

  if (!isValid(parsed) || format(parsed, API_DATE_FORMAT) !== value) {
    return false;
  }

  const minimum = minDashboardDate();
  const today = businessTodayDate();
  return !isBefore(parsed, minimum) && !isAfter(parsed, today);
};
