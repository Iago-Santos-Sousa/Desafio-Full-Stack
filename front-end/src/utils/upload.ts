const DEFAULT_MAX_UPLOAD_BYTES = 2 * 1024 ** 3;

const configuredMaxUploadBytes = Number(import.meta.env.VITE_MAX_UPLOAD_BYTES);

export const MAX_UPLOAD_BYTES =
  Number.isSafeInteger(configuredMaxUploadBytes) && configuredMaxUploadBytes > 0
    ? configuredMaxUploadBytes
    : DEFAULT_MAX_UPLOAD_BYTES;
