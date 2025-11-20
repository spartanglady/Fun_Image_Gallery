import { useState, useMemo } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { PhotoGrid, PhotoGridSkeleton } from './components/PhotoGrid';
import { Lightbox } from './components/Lightbox';
import { UploadZone } from './components/UploadZone';
import { SearchBar } from './components/SearchBar';
import { SortDropdown } from './components/SortDropdown';
import { useInfinitePhotos } from './hooks/useInfinitePhotos';
import { useSearch } from './hooks/useSearch';
import type { PhotoDTO, FilterParams, SortField, SortDirection } from './types';

// Create a client with optimized cache settings
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function GalleryApp() {
  const [selectedPhoto, setSelectedPhoto] = useState<PhotoDTO | null>(null);
  const [filters, setFilters] = useState<FilterParams | null>(null);
  const [isSearching, setIsSearching] = useState(false);
  const [sortBy, setSortBy] = useState<SortField>('captureDate');
  const [sortDir, setSortDir] = useState<SortDirection>('desc');

  // Use either search or infinite photos query based on filter state
  const infiniteQuery = useInfinitePhotos(20, sortBy, sortDir);
  const searchQuery = useSearch(filters || {}, isSearching);

  const activeQuery = isSearching ? searchQuery : infiniteQuery;

  // Flatten paginated data into single array
  const photos = useMemo(() => {
    if (!activeQuery.data) return [];
    return activeQuery.data.pages.flatMap((page) => page.content);
  }, [activeQuery.data]);

  const handleSearch = (newFilters: FilterParams) => {
    setFilters(newFilters);
    setIsSearching(true);
  };

  const handleClearSearch = () => {
    setFilters(null);
    setIsSearching(false);
  };

  const handlePhotoClick = (photo: PhotoDTO) => {
    setSelectedPhoto(photo);
  };

  const handleCloseLightbox = () => {
    setSelectedPhoto(null);
  };

  const handlePreviousPhoto = () => {
    if (!selectedPhoto) return;
    const currentIndex = photos.findIndex((p) => p.id === selectedPhoto.id);
    if (currentIndex > 0) {
      setSelectedPhoto(photos[currentIndex - 1]);
    }
  };

  const handleNextPhoto = () => {
    if (!selectedPhoto) return;
    const currentIndex = photos.findIndex((p) => p.id === selectedPhoto.id);
    if (currentIndex < photos.length - 1) {
      setSelectedPhoto(photos[currentIndex + 1]);
    }
  };

  const handleLoadMore = () => {
    if (activeQuery.hasNextPage && !activeQuery.isFetchingNextPage) {
      activeQuery.fetchNextPage();
    }
  };

  const handleSortChange = (newSortBy: SortField, newSortDir: SortDirection) => {
    setSortBy(newSortBy);
    setSortDir(newSortDir);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <svg
                className="w-8 h-8 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Photo Gallery</h1>
                <p className="text-sm text-gray-500">
                  {photos.length} {photos.length === 1 ? 'photo' : 'photos'}
                  {isSearching && ' (filtered)'}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-4">
              {activeQuery.isLoading && (
                <div className="flex items-center gap-2 text-gray-500">
                  <div className="w-4 h-4 border-2 border-gray-300 border-t-gray-600 rounded-full animate-spin" />
                  <span className="text-sm">Loading...</span>
                </div>
              )}
              <SortDropdown
                onSortChange={handleSortChange}
                currentSortBy={sortBy}
                currentSortDir={sortDir}
              />
            </div>
          </div>
        </div>
      </header>

      {/* Search Bar */}
      <SearchBar onSearch={handleSearch} onClear={handleClearSearch} />

      {/* Main Content */}
      <main className="max-w-7xl mx-auto">
        {activeQuery.isLoading ? (
          <PhotoGridSkeleton />
        ) : activeQuery.isError ? (
          <div className="flex flex-col items-center justify-center h-96 text-red-500">
            <svg
              className="w-16 h-16 mb-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p className="text-lg font-medium">Error loading photos</p>
            <p className="text-sm text-gray-500">{activeQuery.error?.message}</p>
            <button
              onClick={() => activeQuery.refetch()}
              className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Try Again
            </button>
          </div>
        ) : (
          <PhotoGrid
            photos={photos}
            onPhotoClick={handlePhotoClick}
            onLoadMore={handleLoadMore}
            hasMore={activeQuery.hasNextPage}
            isLoading={activeQuery.isFetchingNextPage}
          />
        )}

        {/* Loading indicator for next page */}
        {activeQuery.isFetchingNextPage && (
          <div className="flex items-center justify-center py-8">
            <div className="w-8 h-8 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin" />
          </div>
        )}
      </main>

      {/* Lightbox */}
      <Lightbox
        photo={selectedPhoto}
        onClose={handleCloseLightbox}
        onPrevious={selectedPhoto ? handlePreviousPhoto : undefined}
        onNext={selectedPhoto ? handleNextPhoto : undefined}
      />

      {/* Upload Zone */}
      <UploadZone />
    </div>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <GalleryApp />
    </QueryClientProvider>
  );
}

export default App;
