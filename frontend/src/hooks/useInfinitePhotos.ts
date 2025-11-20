import { useInfiniteQuery } from '@tanstack/react-query';
import { getPhotos } from '../api/client';
import type { PageResponse, PhotoDTO, SortField, SortDirection } from '../types';

export const useInfinitePhotos = (
  size: number = 20,
  sortBy: SortField = 'captureDate',
  sortDir: SortDirection = 'desc'
) => {
  return useInfiniteQuery<PageResponse<PhotoDTO>, Error>({
    queryKey: ['photos', 'infinite', size, sortBy, sortDir],
    queryFn: ({ pageParam = 0 }) => getPhotos(pageParam as number, size, sortBy, sortDir),
    getNextPageParam: (lastPage) => {
      if (lastPage.last) return undefined;
      return lastPage.number + 1;
    },
    initialPageParam: 0,
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
};
