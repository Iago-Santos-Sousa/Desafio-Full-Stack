import { once } from "node:events";
import { createWriteStream } from "node:fs";
import { openAsBlob } from "node:fs";
import { basename, resolve } from "node:path";

const rows = Number(process.env.ROWS ?? process.argv[2] ?? 1_000_000);
const output = resolve(
  process.env.OUTPUT ?? process.argv[3] ?? `/data/benchmark-${rows}.csv`,
);
const apiUrl = process.env.API_URL ?? "http://host.docker.internal:8080";
const pollMs = Number(process.env.POLL_MS ?? 2_000);

if (!Number.isSafeInteger(rows) || rows < 1) {
  throw new Error("ROWS must be a positive integer");
}

await generateCsv();
const uploadStartedAt = Date.now();
const file = await openAsBlob(output, { type: "text/csv" });
const form = new FormData();
form.append("file", file, basename(output));

const accepted = await fetch(`${apiUrl}/api/v1/ingestions`, {
  method: "POST",
  body: form,
});

if (!accepted.ok) {
  throw new Error(
    `Upload failed with HTTP ${accepted.status}: ${await accepted.text()}`,
  );
}

const acceptedBody = await accepted.json();
const jobId = acceptedBody.jobId;
const processingStartedAt = Date.now();
let status;

do {
  await new Promise((resolvePromise) => setTimeout(resolvePromise, pollMs));
  const response = await fetch(`${apiUrl}/api/v1/ingestions/${jobId}`);
  if (!response.ok) {
    throw new Error(
      `Status failed with HTTP ${response.status}: ${await response.text()}`,
    );
  }
  status = await response.json();
  console.log(
    JSON.stringify({
      event: "benchmark_progress",
      jobId,
      status: status.status,
      processedRows: status.processedRows,
      invalidRows: status.invalidRows,
    }),
  );
} while (
  !["COMPLETED", "COMPLETED_WITH_ERRORS", "FAILED"].includes(status.status)
);

console.log(
  JSON.stringify({
    event: "benchmark_completed",
    jobId,
    status: status.status,
    rows,
    fileBytes: file.size,
    uploadMs: processingStartedAt - uploadStartedAt,
    processingMs: Date.now() - processingStartedAt,
    processedRows: status.processedRows,
    validRows: status.validRows,
    invalidRows: status.invalidRows,
  }),
);

async function generateCsv() {
  const stream = createWriteStream(output, { encoding: "utf8" });
  stream.write("occurred_at,category,amount,description\n");

  for (let index = 0; index < rows; index += 1) {
    const date = new Date(
      Date.UTC(2025 + (index % 2), index % 12, (index % 28) + 1),
    );
    const line = `${date.toISOString()},category-${index % 8},${(index / 100).toFixed(2)},Synthetic row ${index + 1}\n`;
    if (!stream.write(line)) {
      await once(stream, "drain");
    }
  }

  stream.end();
  await once(stream, "finish");
}
