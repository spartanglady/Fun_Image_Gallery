import React, { useState, useEffect } from 'react';
import type { PhotoDTO } from '../types';
import {
  formatFileSize,
  formatDate,
  formatDimensions,
  formatISO,
  formatCameraModel,
} from '../utils/formatters';

interface LightboxProps {
  photo: PhotoDTO | null;
  onClose: () => void;
  onPrevious?: () => void;
  onNext?: () => void;
}

export const Lightbox: React.FC<LightboxProps> = ({ photo, onClose, onPrevious, onNext }) => {
  const [imageLoaded, setImageLoaded] = useState(false);
  const [showOriginal, setShowOriginal] = useState(false);
  const [showExif, setShowExif] = useState(true);

  useEffect(() => {
    if (!photo) {
      setImageLoaded(false);
      setShowOriginal(false);
      return;
    }

    // Reset state when photo changes
    setImageLoaded(false);
    setShowOriginal(false);

    // Preload the original image in background
    const img = new Image();
    img.src = photo.originalUrl;
  }, [photo]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!photo) return;

      switch (e.key) {
        case 'Escape':
          onClose();
          break;
        case 'ArrowLeft':
          onPrevious?.();
          break;
        case 'ArrowRight':
          onNext?.();
          break;
        case 'e':
        case 'E':
          setShowExif((prev) => !prev);
          break;
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [photo, onClose, onPrevious, onNext]);

  if (!photo) return null;

  const currentImageUrl = showOriginal ? photo.originalUrl : photo.previewUrl;

  return (
    <div
      className="fixed inset-0 z-50 bg-black/95 flex items-center justify-center"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label="Photo viewer"
    >
      {/* Close button */}
      <button
        className="absolute top-4 right-4 z-10 p-2 text-white hover:bg-white/10 rounded-full transition-colors"
        onClick={onClose}
        aria-label="Close lightbox"
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      {/* Previous button */}
      {onPrevious && (
        <button
          className="absolute left-4 top-1/2 -translate-y-1/2 p-3 text-white hover:bg-white/10 rounded-full transition-colors"
          onClick={(e) => {
            e.stopPropagation();
            onPrevious();
          }}
          aria-label="Previous photo"
        >
          <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
      )}

      {/* Next button */}
      {onNext && (
        <button
          className="absolute right-4 top-1/2 -translate-y-1/2 p-3 text-white hover:bg-white/10 rounded-full transition-colors"
          onClick={(e) => {
            e.stopPropagation();
            onNext();
          }}
          aria-label="Next photo"
        >
          <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>
      )}

      {/* Image container */}
      <div
        className="relative max-w-[90vw] max-h-[90vh] flex items-center justify-center"
        onClick={(e) => e.stopPropagation()}
      >
        <img
          src={currentImageUrl}
          alt={photo.originalFilename}
          className={`max-w-full max-h-[90vh] object-contain transition-opacity duration-300 ${
            imageLoaded ? 'opacity-100' : 'opacity-0'
          }`}
          onLoad={() => setImageLoaded(true)}
        />

        {/* Loading indicator */}
        {!imageLoaded && (
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="w-12 h-12 border-4 border-white/20 border-t-white rounded-full animate-spin" />
          </div>
        )}

        {/* Toggle original/preview */}
        <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
          <button
            className="px-4 py-2 bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white rounded-lg text-sm transition-colors"
            onClick={() => setShowOriginal(!showOriginal)}
          >
            {showOriginal ? 'Show Preview' : 'Show Original'}
          </button>
          <button
            className="px-4 py-2 bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white rounded-lg text-sm transition-colors"
            onClick={() => setShowExif(!showExif)}
          >
            {showExif ? 'Hide Info' : 'Show Info'}
          </button>
        </div>
      </div>

      {/* EXIF sidebar */}
      {showExif && (
        <div
          className="absolute right-0 top-0 bottom-0 w-80 bg-black/80 backdrop-blur-sm text-white p-6 overflow-y-auto"
          onClick={(e) => e.stopPropagation()}
        >
          <h3 className="text-lg font-semibold mb-4 pr-12">Photo Details</h3>

          <div className="space-y-4">
            <div>
              <p className="text-xs text-gray-400 uppercase mb-1">Filename</p>
              <p className="text-sm break-all">{photo.originalFilename}</p>
            </div>

            <div>
              <p className="text-xs text-gray-400 uppercase mb-1">Dimensions</p>
              <p className="text-sm">{formatDimensions(photo.width, photo.height)}</p>
            </div>

            <div>
              <p className="text-xs text-gray-400 uppercase mb-1">File Size</p>
              <p className="text-sm">{formatFileSize(photo.fileSize)}</p>
            </div>

            <div>
              <p className="text-xs text-gray-400 uppercase mb-1">Capture Date</p>
              <p className="text-sm">{formatDate(photo.captureDate)}</p>
            </div>

            {photo.cameraModel && (
              <div>
                <p className="text-xs text-gray-400 uppercase mb-1">Camera</p>
                <p className="text-sm">{formatCameraModel(photo.cameraModel)}</p>
              </div>
            )}

            {photo.iso && (
              <div>
                <p className="text-xs text-gray-400 uppercase mb-1">ISO</p>
                <p className="text-sm">{formatISO(photo.iso)}</p>
              </div>
            )}

            {photo.tags.length > 0 && (
              <div>
                <p className="text-xs text-gray-400 uppercase mb-1">Tags</p>
                <div className="flex flex-wrap gap-2 mt-2">
                  {photo.tags.map((tag) => (
                    <span
                      key={tag}
                      className="px-3 py-1 bg-white/10 rounded-full text-sm"
                    >
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="mt-6 pt-6 border-t border-white/10">
            <p className="text-xs text-gray-400">Keyboard shortcuts</p>
            <div className="mt-2 space-y-1 text-sm">
              <p><kbd className="px-2 py-0.5 bg-white/10 rounded text-xs">←</kbd> Previous</p>
              <p><kbd className="px-2 py-0.5 bg-white/10 rounded text-xs">→</kbd> Next</p>
              <p><kbd className="px-2 py-0.5 bg-white/10 rounded text-xs">E</kbd> Toggle info</p>
              <p><kbd className="px-2 py-0.5 bg-white/10 rounded text-xs">Esc</kbd> Close</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
