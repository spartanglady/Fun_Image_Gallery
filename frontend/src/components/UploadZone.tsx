import React, { useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { useUpload } from '../hooks/useUpload';
import type { UploadProgress } from '../types';

export const UploadZone: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { uploads, uploadFiles, clearCompleted } = useUpload();

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: {
      'image/*': ['.jpg', '.jpeg', '.png', '.gif', '.webp'],
    },
    multiple: true,
    onDrop: (acceptedFiles) => {
      if (acceptedFiles.length > 0) {
        uploadFiles(acceptedFiles);
        setIsOpen(true);
      }
    },
  });

  const getStatusColor = (status: UploadProgress['status']) => {
    switch (status) {
      case 'completed':
        return 'bg-green-500';
      case 'error':
        return 'bg-red-500';
      case 'duplicate':
        return 'bg-yellow-500';
      case 'uploading':
      case 'hashing':
      case 'checking':
        return 'bg-blue-500';
      default:
        return 'bg-gray-300';
    }
  };

  const getStatusText = (status: UploadProgress['status']) => {
    switch (status) {
      case 'hashing':
        return 'Hashing...';
      case 'checking':
        return 'Checking duplicate...';
      case 'uploading':
        return 'Uploading...';
      case 'completed':
        return 'Completed';
      case 'error':
        return 'Error';
      case 'duplicate':
        return 'Duplicate';
      default:
        return 'Pending';
    }
  };

  const activeUploads = uploads.filter(
    (u) => u.status !== 'completed' && u.status !== 'error' && u.status !== 'duplicate'
  );

  return (
    <>
      {/* Floating Action Button */}
      <button
        className="fixed bottom-6 right-6 z-40 p-4 bg-blue-600 hover:bg-blue-700 text-white rounded-full shadow-lg transition-all hover:scale-110"
        onClick={() => setIsOpen(true)}
        aria-label="Upload photos"
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 4v16m8-8H4"
          />
        </svg>
        {activeUploads.length > 0 && (
          <span className="absolute -top-1 -right-1 w-6 h-6 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
            {activeUploads.length}
          </span>
        )}
      </button>

      {/* Upload Modal */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[80vh] flex flex-col">
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b">
              <h2 className="text-xl font-semibold">Upload Photos</h2>
              <button
                className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                onClick={() => setIsOpen(false)}
                aria-label="Close upload dialog"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              </button>
            </div>

            {/* Dropzone */}
            <div
              {...getRootProps()}
              className={`m-4 p-8 border-2 border-dashed rounded-lg text-center cursor-pointer transition-colors ${
                isDragActive
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-300 hover:border-gray-400'
              }`}
            >
              <input {...getInputProps()} />
              <svg
                className="w-12 h-12 mx-auto mb-4 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
                />
              </svg>
              {isDragActive ? (
                <p className="text-blue-600 font-medium">Drop the files here...</p>
              ) : (
                <>
                  <p className="text-gray-700 font-medium mb-1">
                    Drag and drop photos here, or click to select
                  </p>
                  <p className="text-sm text-gray-500">Supports JPG, PNG, GIF, WebP</p>
                </>
              )}
            </div>

            {/* Upload Progress List */}
            {uploads.length > 0 && (
              <div className="flex-1 overflow-y-auto p-4 space-y-2">
                <div className="flex items-center justify-between mb-2">
                  <h3 className="font-medium text-sm text-gray-700">
                    Uploads ({uploads.length})
                  </h3>
                  {uploads.some((u) => u.status === 'completed' || u.status === 'error' || u.status === 'duplicate') && (
                    <button
                      className="text-sm text-blue-600 hover:text-blue-700"
                      onClick={clearCompleted}
                    >
                      Clear completed
                    </button>
                  )}
                </div>

                {uploads.map((upload, index) => (
                  <div key={index} className="border rounded-lg p-3">
                    <div className="flex items-center justify-between mb-2">
                      <p className="text-sm font-medium truncate flex-1 mr-2">
                        {upload.file.name}
                      </p>
                      <span className="text-xs text-gray-500">{getStatusText(upload.status)}</span>
                    </div>

                    {/* Progress bar */}
                    <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                      <div
                        className={`h-full transition-all duration-300 ${getStatusColor(
                          upload.status
                        )}`}
                        style={{ width: `${upload.progress}%` }}
                      />
                    </div>

                    {/* Error message */}
                    {upload.status === 'error' && upload.error && (
                      <p className="text-xs text-red-600 mt-1">{upload.error}</p>
                    )}

                    {/* Duplicate message */}
                    {upload.status === 'duplicate' && (
                      <p className="text-xs text-yellow-600 mt-1">
                        This photo already exists in your gallery
                      </p>
                    )}
                  </div>
                ))}
              </div>
            )}

            {/* Footer */}
            <div className="p-4 border-t bg-gray-50 rounded-b-lg">
              <p className="text-xs text-gray-500 text-center">
                Photos are automatically checked for duplicates using SHA-256 hashing
              </p>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
