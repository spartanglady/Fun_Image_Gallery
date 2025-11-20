import { useQuery } from '@tanstack/react-query';
import { getPhotos } from '../api/client';
import type { PageResponse, PhotoDTO } from '../types';

export const usePhotos = (page: number = 0, size: number = 20) => {
  return useQuery<PageResponse<PhotoDTO>, Error>({
    queryKey: ['photos', page, size],
    queryFn: () => getPhotos(page, size),
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
  });
};
