/**
 * Format file size in human-readable format
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${Math.round((bytes / Math.pow(k, i)) * 100) / 100} ${sizes[i]}`;
};

/**
 * Format date in human-readable format
 */
export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  // Less than a minute
  if (diffInSeconds < 60) {
    return 'just now';
  }

  // Less than an hour
  if (diffInSeconds < 3600) {
    const minutes = Math.floor(diffInSeconds / 60);
    return `${minutes} ${minutes === 1 ? 'minute' : 'minutes'} ago`;
  }

  // Less than a day
  if (diffInSeconds < 86400) {
    const hours = Math.floor(diffInSeconds / 3600);
    return `${hours} ${hours === 1 ? 'hour' : 'hours'} ago`;
  }

  // Less than a week
  if (diffInSeconds < 604800) {
    const days = Math.floor(diffInSeconds / 86400);
    return `${days} ${days === 1 ? 'day' : 'days'} ago`;
  }

  // Format as date
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

/**
 * Format date for input fields (YYYY-MM-DD)
 */
export const formatDateForInput = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/**
 * Format dimensions
 */
export const formatDimensions = (width: number, height: number): string => {
  return `${width} × ${height}`;
};

/**
 * Format ISO value
 */
export const formatISO = (iso: string | null): string => {
  if (!iso) return 'N/A';
  return `ISO ${iso}`;
};

/**
 * Format camera model
 */
export const formatCameraModel = (model: string | null): string => {
  if (!model) return 'Unknown Camera';
  return model;
};

/**
 * Truncate filename with extension
 */
export const truncateFilename = (filename: string, maxLength: number = 30): string => {
  if (filename.length <= maxLength) return filename;

  const extension = filename.split('.').pop() || '';
  const nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
  const availableLength = maxLength - extension.length - 4; // 4 for "..." and "."

  if (availableLength < 1) return filename.substring(0, maxLength) + '...';

  return `${nameWithoutExt.substring(0, availableLength)}...${extension}`;
};
