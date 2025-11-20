import React, { useMemo } from 'react';
import { VirtuosoGrid } from 'react-virtuoso';
import { PhotoCard } from './PhotoCard';
import type { PhotoDTO } from '../types';

interface PhotoGridProps {
  photos: PhotoDTO[];
  onPhotoClick: (photo: PhotoDTO) => void;
  onLoadMore?: () => void;
  hasMore?: boolean;
  isLoading?: boolean;
}

export const PhotoGrid: React.FC<PhotoGridProps> = ({
  photos,
  onPhotoClick,
  onLoadMore,
  hasMore,
  isLoading,
}) => {
  const gridComponents = useMemo(
    () => ({
      List: React.forwardRef<HTMLDivElement>((props, ref) => (
        <div
          ref={ref}
          {...props}
          className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 p-4"
        />
      )),
      Item: ({ children, ...props }: React.HTMLAttributes<HTMLDivElement>) => (
        <div {...props}>{children}</div>
      ),
    }),
    []
  );

  if (photos.length === 0 && !isLoading) {
    return (
      <div className="flex flex-col items-center justify-center h-96 text-gray-500">
        <svg
          className="w-24 h-24 mb-4 text-gray-300"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
          />
        </svg>
        <p className="text-lg font-medium">No photos found</p>
        <p className="text-sm">Upload some photos to get started</p>
      </div>
    );
  }

  return (
    <VirtuosoGrid
      style={{ height: 'calc(100vh - 200px)' }}
      totalCount={photos.length}
      components={gridComponents}
      endReached={() => {
        if (hasMore && !isLoading && onLoadMore) {
          onLoadMore();
        }
      }}
      itemContent={(index) => {
        const photo = photos[index];
        return <PhotoCard photo={photo} onClick={() => onPhotoClick(photo)} />;
      }}
    />
  );
};

// Loading skeleton component
export const PhotoGridSkeleton: React.FC = () => {
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 p-4">
      {Array.from({ length: 24 }).map((_, i) => (
        <div
          key={i}
          className="aspect-square bg-gray-200 rounded-lg animate-pulse"
          aria-hidden="true"
        />
      ))}
    </div>
  );
};
