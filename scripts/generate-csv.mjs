import { mkdir } from "node:fs/promises";
import { createWriteStream } from "node:fs";
import { once } from "node:events";

const total = Number(process.argv[2] ?? 1_000_000);
const output = process.argv[3] ?? "/data/transactions.csv";

if (!Number.isSafeInteger(total) || total < 1) {
  throw new Error("Row count must be positive integer");
}

const slash = output.lastIndexOf("/");
await mkdir(slash > 0 ? output.slice(0, slash) : ".", { recursive: true });

const stream = createWriteStream(output, { encoding: "utf8" });

stream.write("occurred_at,category,amount,description\n");

for (let i = 0; i < total; i += 1) {
  const date = new Date(
    Date.UTC(2025 + (i % 2), i % 12, (i % 28) + 1, i % 24, i % 60),
  );

  const line = `${date.toISOString()},${["food", "transport", "health", "utilities"][i % 4]},${((i % 100000) / 100 + 1).toFixed(2)},Synthetic transaction ${i + 1}\n`;

  if (!stream.write(line)) await once(stream, "drain");
}

stream.end();

await once(stream, "finish");

console.log(`Generated ${total} rows at ${output}`);
