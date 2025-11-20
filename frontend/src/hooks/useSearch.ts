import { useInfiniteQuery } from '@tanstack/react-query';
import { searchPhotos } from '../api/client';
import type { FilterParams, PageResponse, PhotoDTO } from '../types';

export const useSearch = (filters: FilterParams, enabled: boolean = true) => {
  return useInfiniteQuery<PageResponse<PhotoDTO>, Error>({
    queryKey: ['photos', 'search', filters],
    queryFn: ({ pageParam = 0 }) =>
      searchPhotos({ ...filters, page: pageParam as number, size: filters.size || 20 }),
    getNextPageParam: (lastPage) => {
      if (lastPage.last) return undefined;
      return lastPage.number + 1;
    },
    initialPageParam: 0,
    enabled,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};
