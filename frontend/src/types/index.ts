// Photo entity from backend
export interface Photo {
  id: string;
  originalFilename: string;
  storedPath: string;
  fileSize: number;
  mimeType: string;
  captureDate: string; // ISO date string
  width: number;
  height: number;
  cameraModel: string | null;
  iso: string | null;
  tags: string[];
  fileHash: string;
  uploadedAt: string; // ISO date string
}

// DTO for photo list responses
export interface PhotoDTO {
  id: string;
  originalFilename: string;
  fileSize: number;
  mimeType: string;
  captureDate: string;
  width: number;
  height: number;
  cameraModel: string | null;
  iso: string | null;
  tags: string[];
  thumbnailUrl: string;
  previewUrl: string;
  originalUrl: string;
}

// Paginated response from backend
export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
}

// Sort options
export type SortField = 'captureDate' | 'originalFilename' | 'fileSize' | 'uploadedAt';
export type SortDirection = 'asc' | 'desc';

export interface SortParams {
  sortBy: SortField;
  sortDir: SortDirection;
}

// Filter parameters for search
export interface FilterParams {
  tags?: string[];
  startDate?: string; // ISO date string
  endDate?: string; // ISO date string
  cameraModel?: string;
  query?: string; // filename search
  page?: number;
  size?: number;
  sortBy?: SortField;
  sortDir?: SortDirection;
}

// Upload progress tracking
export interface UploadProgress {
  file: File;
  progress: number;
  status: 'pending' | 'hashing' | 'checking' | 'uploading' | 'completed' | 'error' | 'duplicate';
  error?: string;
  duplicatePhotoId?: string;
  photoId?: string;
}

// Duplicate check request/response
export interface DuplicateCheckRequest {
  fileHash: string;
  filename: string;
}

export interface DuplicateCheckResponse {
  isDuplicate: boolean;
  existingPhotoId?: string;
  existingPhoto?: PhotoDTO;
}

// Image type for loading different versions
export type ImageType = 'thumbnail' | 'preview' | 'original';

// EXIF data display
export interface ExifData {
  cameraModel: string | null;
  iso: string | null;
  captureDate: string;
  width: number;
  height: number;
  fileSize: number;
}
