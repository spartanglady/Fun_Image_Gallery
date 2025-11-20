import axios from 'axios';
import type {
  PhotoDTO,
  PageResponse,
  FilterParams,
  DuplicateCheckRequest,
  DuplicateCheckResponse
} from '../types';
import { generateMockPageResponse, mockPhotos } from './mockData';

// Toggle this to switch between mock and real API
const USE_MOCK_DATA = true;

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Helper to simulate API delay for realistic mock behavior
const mockDelay = (ms: number = 500) => new Promise(resolve => setTimeout(resolve, ms));

// Get paginated photos
export const getPhotos = async (
  page: number = 0,
  size: number = 20,
  sortBy: string = 'captureDate',
  sortDir: string = 'desc'
): Promise<PageResponse<PhotoDTO>> => {
  if (USE_MOCK_DATA) {
    await mockDelay();
    const mockData = generateMockPageResponse(page, size);

    // Apply sorting to mock data
    const sorted = [...mockData.content].sort((a, b) => {
      let comparison = 0;

      switch (sortBy) {
        case 'originalFilename':
          comparison = a.originalFilename.localeCompare(b.originalFilename);
          break;
        case 'fileSize':
          comparison = a.fileSize - b.fileSize;
          break;
        case 'captureDate':
          comparison = new Date(a.captureDate).getTime() - new Date(b.captureDate).getTime();
          break;
        default:
          comparison = 0;
      }

      return sortDir === 'asc' ? comparison : -comparison;
    });

    return { ...mockData, content: sorted };
  }

  const response = await apiClient.get<PageResponse<PhotoDTO>>('/api/photos', {
    params: { page, size, sortBy, sortDir },
  });
  return response.data;
};

// Search photos with filters
export const searchPhotos = async (filters: FilterParams): Promise<PageResponse<PhotoDTO>> => {
  if (USE_MOCK_DATA) {
    await mockDelay();
    let filtered = [...mockPhotos];

    // Apply filters
    if (filters.tags && filters.tags.length > 0) {
      filtered = filtered.filter(photo =>
        filters.tags!.some(tag => photo.tags.includes(tag))
      );
    }

    if (filters.cameraModel) {
      filtered = filtered.filter(photo => photo.cameraModel === filters.cameraModel);
    }

    if (filters.query) {
      filtered = filtered.filter(photo =>
        photo.originalFilename.toLowerCase().includes(filters.query!.toLowerCase())
      );
    }

    if (filters.startDate) {
      filtered = filtered.filter(photo =>
        new Date(photo.captureDate) >= new Date(filters.startDate!)
      );
    }

    if (filters.endDate) {
      filtered = filtered.filter(photo =>
        new Date(photo.captureDate) <= new Date(filters.endDate!)
      );
    }

    return generateMockPageResponse(filters.page || 0, filters.size || 20, filtered);
  }

  const response = await apiClient.get<PageResponse<PhotoDTO>>('/api/photos/search', {
    params: filters,
  });
  return response.data;
};

// Get photo image URL by type
export const getPhotoImageUrl = (photoId: string, type: 'thumbnail' | 'preview' | 'original'): string => {
  if (USE_MOCK_DATA) {
    const photo = mockPhotos.find(p => p.id === photoId);
    if (!photo) return '';

    switch (type) {
      case 'thumbnail':
        return photo.thumbnailUrl;
      case 'preview':
        return photo.previewUrl;
      case 'original':
        return photo.originalUrl;
      default:
        return photo.thumbnailUrl;
    }
  }

  return `${import.meta.env.VITE_API_BASE_URL}/api/photos/${photoId}/image?type=${type}`;
};

// Check for duplicate photo by hash
export const checkDuplicate = async (request: DuplicateCheckRequest): Promise<DuplicateCheckResponse> => {
  if (USE_MOCK_DATA) {
    await mockDelay(300);
    // Simulate 10% duplicate rate
    const isDuplicate = Math.random() < 0.1;

    if (isDuplicate && mockPhotos.length > 0) {
      const existingPhoto = mockPhotos[0];
      return {
        isDuplicate: true,
        existingPhotoId: existingPhoto.id,
        existingPhoto,
      };
    }

    return { isDuplicate: false };
  }

  const response = await apiClient.post<DuplicateCheckResponse>('/api/photos/check-duplicate', request);
  return response.data;
};

// Upload photo
export const uploadPhoto = async (
  file: File,
  onProgress?: (progress: number) => void
): Promise<PhotoDTO> => {
  if (USE_MOCK_DATA) {
    // Simulate upload progress
    for (let i = 0; i <= 100; i += 10) {
      await mockDelay(100);
      onProgress?.(i);
    }

    // Generate a new mock photo
    const newPhoto: PhotoDTO = {
      id: `photo-new-${Date.now()}`,
      originalFilename: file.name,
      fileSize: file.size,
      mimeType: file.type,
      captureDate: new Date().toISOString(),
      width: 4000,
      height: 3000,
      cameraModel: 'Mock Camera',
      iso: '200',
      tags: [],
      thumbnailUrl: URL.createObjectURL(file),
      previewUrl: URL.createObjectURL(file),
      originalUrl: URL.createObjectURL(file),
    };

    return newPhoto;
  }

  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post<PhotoDTO>('/api/photos/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      if (progressEvent.total) {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        onProgress?.(progress);
      }
    },
  });

  return response.data;
};

// Add tags to a photo
export const addTags = async (photoId: string, tags: string[]): Promise<PhotoDTO> => {
  if (USE_MOCK_DATA) {
    await mockDelay();
    const photo = mockPhotos.find(p => p.id === photoId);
    if (!photo) throw new Error('Photo not found');

    photo.tags = [...new Set([...photo.tags, ...tags])];
    return photo;
  }

  const response = await apiClient.post<PhotoDTO>(`/api/photos/${photoId}/tags`, { tags });
  return response.data;
};

export { USE_MOCK_DATA };
