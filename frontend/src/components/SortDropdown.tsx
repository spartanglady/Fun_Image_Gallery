import { useState } from 'react';
import type { SortField, SortDirection } from '../types';

interface SortDropdownProps {
  onSortChange: (sortBy: SortField, sortDir: SortDirection) => void;
  currentSortBy: SortField;
  currentSortDir: SortDirection;
}

const sortOptions: { value: SortField; label: string }[] = [
  { value: 'captureDate', label: 'Date Taken' },
  { value: 'originalFilename', label: 'File Name' },
  { value: 'fileSize', label: 'File Size' },
  { value: 'uploadedAt', label: 'Upload Date' },
];

export const SortDropdown = ({ onSortChange, currentSortBy, currentSortDir }: SortDropdownProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const handleSortByChange = (sortBy: SortField) => {
    onSortChange(sortBy, currentSortDir);
    setIsOpen(false);
  };

  const toggleDirection = () => {
    onSortChange(currentSortBy, currentSortDir === 'asc' ? 'desc' : 'asc');
  };

  const currentLabel = sortOptions.find(opt => opt.value === currentSortBy)?.label || 'Date Taken';

  return (
    <div className="relative inline-block">
      <div className="flex items-center gap-2">
        {/* Sort By Dropdown */}
        <div className="relative">
          <button
            onClick={() => setIsOpen(!isOpen)}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium text-gray-700"
          >
            <svg
              className="w-4 h-4 text-gray-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 4h13M3 8h9m-9 4h6m4 0l4-4m0 0l4 4m-4-4v12"
              />
            </svg>
            <span>Sort: {currentLabel}</span>
            <svg
              className={`w-4 h-4 text-gray-500 transition-transform ${isOpen ? 'rotate-180' : ''}`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          {isOpen && (
            <>
              <div
                className="fixed inset-0 z-10"
                onClick={() => setIsOpen(false)}
              />
              <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-20">
                {sortOptions.map((option) => (
                  <button
                    key={option.value}
                    onClick={() => handleSortByChange(option.value)}
                    className={`w-full text-left px-4 py-2 text-sm hover:bg-gray-50 first:rounded-t-lg last:rounded-b-lg transition-colors ${
                      currentSortBy === option.value
                        ? 'bg-blue-50 text-blue-700 font-medium'
                        : 'text-gray-700'
                    }`}
                  >
                    {option.label}
                  </button>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Direction Toggle */}
        <button
          onClick={toggleDirection}
          className="p-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          title={currentSortDir === 'asc' ? 'Ascending' : 'Descending'}
        >
          {currentSortDir === 'asc' ? (
            <svg
              className="w-5 h-5 text-gray-700"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4"
              />
            </svg>
          ) : (
            <svg
              className="w-5 h-5 text-gray-700"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 4h13M3 8h9m-9 4h6m4 0l4-4m0 0l4 4m-4-4v12"
              />
            </svg>
          )}
        </button>
      </div>
    </div>
  );
};
