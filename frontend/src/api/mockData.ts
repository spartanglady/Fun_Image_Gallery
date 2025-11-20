import type { PhotoDTO, PageResponse } from '../types';

// Mock photo data for development
export const mockPhotos: PhotoDTO[] = Array.from({ length: 50 }, (_, i) => ({
  id: `photo-${i + 1}`,
  originalFilename: `photo-${i + 1}.jpg`,
  fileSize: Math.floor(Math.random() * 5000000) + 1000000, // 1-6 MB
  mimeType: 'image/jpeg',
  captureDate: new Date(Date.now() - Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString(),
  width: 4000,
  height: 3000,
  cameraModel: ['Canon EOS 5D Mark IV', 'Nikon D850', 'Sony A7R III', null][Math.floor(Math.random() * 4)],
  iso: ['100', '200', '400', '800', null][Math.floor(Math.random() * 5)],
  tags: ['nature', 'landscape', 'portrait', 'architecture'].slice(0, Math.floor(Math.random() * 3) + 1),
  thumbnailUrl: `https://picsum.photos/seed/${i + 1}/300/300`,
  previewUrl: `https://picsum.photos/seed/${i + 1}/1280/960`,
  originalUrl: `https://picsum.photos/seed/${i + 1}/4000/3000`,
}));

export const generateMockPageResponse = (
  page: number = 0,
  size: number = 20,
  photos: PhotoDTO[] = mockPhotos
): PageResponse<PhotoDTO> => {
  const start = page * size;
  const end = start + size;
  const content = photos.slice(start, end);

  return {
    content,
    pageable: {
      pageNumber: page,
      pageSize: size,
      sort: {
        sorted: false,
        unsorted: true,
        empty: true,
      },
      offset: start,
      paged: true,
      unpaged: false,
    },
    totalPages: Math.ceil(photos.length / size),
    totalElements: photos.length,
    last: end >= photos.length,
    first: page === 0,
    size,
    number: page,
    numberOfElements: content.length,
    empty: content.length === 0,
  };
};
