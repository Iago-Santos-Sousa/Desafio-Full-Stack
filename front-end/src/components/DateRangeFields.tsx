import { Stack } from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import type { Control, FieldPath, UseFormGetValues } from "react-hook-form";
import { Controller } from "react-hook-form";
import {
  businessTodayDate,
  minDashboardDate,
  type DashboardDateFormValues,
  validateDashboardDate,
} from "@/utils/dateRange";

type DateFieldName = FieldPath<DashboardDateFormValues>;

export function DateRangeFields({
  control,
  getValues,
}: {
  control: Control<DashboardDateFormValues>;
  getValues: UseFormGetValues<DashboardDateFormValues>;
}) {
  const renderField = (name: DateFieldName, label: string) => (
    <Controller
      control={control}
      name={name}
      rules={{
        required: "Informe uma data.",
        validate: (value) => {
          const validation = validateDashboardDate(value);
          if (validation !== true) return validation;

          if (name === "to") {
            const from = getValues("from");
            if (from && value && from > value) {
              return "A data final deve ser igual ou posterior à inicial.";
            }
          }

          return true;
        },
      }}
      render={({ field, fieldState }) => (
        <DatePicker
          label={label}
          format="dd/MM/yyyy"
          value={field.value}
          minDate={minDashboardDate()}
          maxDate={businessTodayDate()}
          onChange={(value) => field.onChange(value)}
          onError={() => field.onChange(null)}
          slotProps={{
            field: {
              readOnly: true,
            },
            textField: {
              size: "small",
              onBlur: field.onBlur,
              error: Boolean(fieldState.error),
              helperText: fieldState.error?.message,
            },
          }}
        />
      )}
    />
  );

  return (
    <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
      {renderField("from", "Início")}
      {renderField("to", "Fim")}
    </Stack>
  );
}
