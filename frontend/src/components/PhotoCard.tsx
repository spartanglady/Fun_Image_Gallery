import React from 'react';
import type { PhotoDTO } from '../types';
import { formatFileSize, formatDate } from '../utils/formatters';

interface PhotoCardProps {
  photo: PhotoDTO;
  onClick: () => void;
}

export const PhotoCard: React.FC<PhotoCardProps> = ({ photo, onClick }) => {
  return (
    <div
      className="group relative aspect-square overflow-hidden rounded-lg bg-gray-200 cursor-pointer transition-transform hover:scale-105"
      onClick={onClick}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onClick();
        }
      }}
      aria-label={`View ${photo.originalFilename}`}
    >
      {/* Image */}
      <img
        src={photo.thumbnailUrl}
        alt={photo.originalFilename}
        loading="lazy"
        className="h-full w-full object-cover transition-opacity duration-200"
      />

      {/* Overlay on hover */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-200">
        <div className="absolute bottom-0 left-0 right-0 p-3 text-white">
          <p className="text-sm font-medium truncate mb-1">{photo.originalFilename}</p>
          <div className="flex items-center justify-between text-xs">
            <span>{formatFileSize(photo.fileSize)}</span>
            <span>{formatDate(photo.captureDate)}</span>
          </div>
          {photo.tags.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-2">
              {photo.tags.slice(0, 3).map((tag) => (
                <span
                  key={tag}
                  className="px-2 py-0.5 bg-white/20 backdrop-blur-sm rounded-full text-xs"
                >
                  {tag}
                </span>
              ))}
              {photo.tags.length > 3 && (
                <span className="px-2 py-0.5 bg-white/20 backdrop-blur-sm rounded-full text-xs">
                  +{photo.tags.length - 3}
                </span>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Loading skeleton */}
      <div className="absolute inset-0 bg-gray-300 animate-pulse opacity-0 group-[.loading]:opacity-100" />
    </div>
  );
};
