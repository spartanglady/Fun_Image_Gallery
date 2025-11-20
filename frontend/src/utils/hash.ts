/**
 * Calculate SHA-256 hash of a file using the Web Crypto API
 * Used for client-side duplicate detection before upload
 */
export const calculateFileHash = async (file: File): Promise<string> => {
  const arrayBuffer = await file.arrayBuffer();
  const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  return hashHex;
};

/**
 * Calculate hash with progress callback for large files
 * Processes file in chunks to avoid blocking the UI
 */
export const calculateFileHashWithProgress = async (
  file: File,
  onProgress?: (progress: number) => void
): Promise<string> => {
  const CHUNK_SIZE = 1024 * 1024; // 1MB chunks
  const chunks = Math.ceil(file.size / CHUNK_SIZE);
  let currentChunk = 0;

  // For small files, use the simple method
  if (chunks <= 1) {
    const hash = await calculateFileHash(file);
    onProgress?.(100);
    return hash;
  }

  // For larger files, process in chunks
  const hashParts: ArrayBuffer[] = [];

  for (let i = 0; i < file.size; i += CHUNK_SIZE) {
    const chunk = file.slice(i, i + CHUNK_SIZE);
    const arrayBuffer = await chunk.arrayBuffer();
    hashParts.push(arrayBuffer);

    currentChunk++;
    const progress = Math.round((currentChunk / chunks) * 100);
    onProgress?.(progress);
  }

  // Combine all chunks and hash the result
  const totalBuffer = new Uint8Array(file.size);
  let offset = 0;
  for (const part of hashParts) {
    totalBuffer.set(new Uint8Array(part), offset);
    offset += part.byteLength;
  }

  const hashBuffer = await crypto.subtle.digest('SHA-256', totalBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

  return hashHex;
};
