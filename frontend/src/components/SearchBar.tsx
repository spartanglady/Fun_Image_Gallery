import React, { useState, useMemo } from 'react';
import type { FilterParams } from '../types';
import { formatDateForInput } from '../utils/formatters';

interface SearchBarProps {
  onSearch: (filters: FilterParams) => void;
  onClear: () => void;
}

export const SearchBar: React.FC<SearchBarProps> = ({ onSearch, onClear }) => {
  const [query, setQuery] = useState('');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [cameraModel, setCameraModel] = useState('');
  const [showAdvanced, setShowAdvanced] = useState(false);

  // Available tags (in production, this would come from the API)
  const availableTags = useMemo(
    () => ['nature', 'landscape', 'portrait', 'architecture', 'wildlife', 'urban', 'sunset'],
    []
  );

  // Available camera models (in production, this would come from the API)
  const availableCameras = useMemo(
    () => ['Canon EOS 5D Mark IV', 'Nikon D850', 'Sony A7R III', 'Fujifilm X-T4'],
    []
  );

  const handleSearch = () => {
    const filters: FilterParams = {};

    if (query.trim()) filters.query = query.trim();
    if (selectedTags.length > 0) filters.tags = selectedTags;
    if (startDate) filters.startDate = new Date(startDate).toISOString();
    if (endDate) filters.endDate = new Date(endDate).toISOString();
    if (cameraModel) filters.cameraModel = cameraModel;

    onSearch(filters);
  };

  const handleClear = () => {
    setQuery('');
    setSelectedTags([]);
    setStartDate('');
    setEndDate('');
    setCameraModel('');
    onClear();
  };

  const toggleTag = (tag: string) => {
    setSelectedTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    );
  };

  const hasActiveFilters =
    query || selectedTags.length > 0 || startDate || endDate || cameraModel;

  return (
    <div className="bg-white border-b shadow-sm">
      <div className="max-w-7xl mx-auto p-4">
        {/* Main search bar */}
        <div className="flex gap-2">
          <div className="flex-1 relative">
            <input
              type="text"
              placeholder="Search by filename..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleSearch();
              }}
              className="w-full px-4 py-2 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
            />
            <svg
              className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
          </div>

          <button
            onClick={() => setShowAdvanced(!showAdvanced)}
            className={`px-4 py-2 border rounded-lg transition-colors ${
              showAdvanced || hasActiveFilters
                ? 'bg-blue-50 border-blue-500 text-blue-700'
                : 'border-gray-300 hover:bg-gray-50'
            }`}
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
              />
            </svg>
          </button>

          <button
            onClick={handleSearch}
            className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors font-medium"
          >
            Search
          </button>

          {hasActiveFilters && (
            <button
              onClick={handleClear}
              className="px-4 py-2 border border-gray-300 hover:bg-gray-50 rounded-lg transition-colors"
            >
              Clear
            </button>
          )}
        </div>

        {/* Advanced filters */}
        {showAdvanced && (
          <div className="mt-4 space-y-4 p-4 bg-gray-50 rounded-lg">
            {/* Tags filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Tags</label>
              <div className="flex flex-wrap gap-2">
                {availableTags.map((tag) => (
                  <button
                    key={tag}
                    onClick={() => toggleTag(tag)}
                    className={`px-3 py-1 rounded-full text-sm transition-colors ${
                      selectedTags.includes(tag)
                        ? 'bg-blue-600 text-white'
                        : 'bg-white border border-gray-300 hover:bg-gray-50'
                    }`}
                  >
                    {tag}
                  </button>
                ))}
              </div>
            </div>

            {/* Date range filter */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Start Date
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  max={endDate || formatDateForInput(new Date())}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">End Date</label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  min={startDate}
                  max={formatDateForInput(new Date())}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                />
              </div>
            </div>

            {/* Camera model filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Camera Model
              </label>
              <select
                value={cameraModel}
                onChange={(e) => setCameraModel(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
              >
                <option value="">All cameras</option>
                {availableCameras.map((camera) => (
                  <option key={camera} value={camera}>
                    {camera}
                  </option>
                ))}
              </select>
            </div>
          </div>
        )}

        {/* Active filter chips */}
        {hasActiveFilters && (
          <div className="mt-3 flex flex-wrap gap-2">
            {query && (
              <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm flex items-center gap-1">
                Query: {query}
                <button onClick={() => setQuery('')} className="hover:text-blue-900">
                  <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path
                      fillRule="evenodd"
                      d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                      clipRule="evenodd"
                    />
                  </svg>
                </button>
              </span>
            )}
            {selectedTags.map((tag) => (
              <span
                key={tag}
                className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm flex items-center gap-1"
              >
                {tag}
                <button onClick={() => toggleTag(tag)} className="hover:text-blue-900">
                  <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path
                      fillRule="evenodd"
                      d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                      clipRule="evenodd"
                    />
                  </svg>
                </button>
              </span>
            ))}
            {cameraModel && (
              <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm flex items-center gap-1">
                Camera: {cameraModel}
                <button onClick={() => setCameraModel('')} className="hover:text-blue-900">
                  <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path
                      fillRule="evenodd"
                      d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                      clipRule="evenodd"
                    />
                  </svg>
                </button>
              </span>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
