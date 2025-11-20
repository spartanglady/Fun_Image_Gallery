# Photo Gallery Frontend

A high-performance React frontend for the Photo Gallery application built with Vite, TypeScript, and Tailwind CSS.

## Features

- **Virtualized Photo Grid**: Efficient rendering of large photo collections using react-virtuoso
- **Infinite Scroll**: Automatic loading of more photos as you scroll
- **Advanced Search**: Filter by tags, date range, camera model, and filename
- **Lightbox Viewer**: Full-screen photo viewer with keyboard navigation and EXIF data display
- **Drag & Drop Upload**: Multi-file upload with progress tracking
- **Duplicate Detection**: Client-side SHA-256 hashing to prevent duplicate uploads
- **Progressive Image Loading**: Preview images load first, followed by original quality
- **Responsive Design**: Mobile-friendly UI with Tailwind CSS

## Tech Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **TanStack Query (React Query v5)** - Data fetching and caching
- **Tailwind CSS** - Styling
- **react-virtuoso** - Virtual scrolling for performance
- **react-dropzone** - File upload handling
- **axios** - HTTP client

## Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

```bash
# Install dependencies
npm install
```

### Development

```bash
# Start development server (runs on http://localhost:5173)
npm run dev
```

The dev server includes:
- Hot Module Replacement (HMR)
- TypeScript type checking
- Proxy to backend API (http://localhost:8080)

### Build for Production

```bash
# Build optimized production bundle
npm run build

# Preview production build locally
npm run preview
```

## Project Structure

```
src/
├── api/              # API client and mock data
│   ├── client.ts     # Axios client and API functions
│   └── mockData.ts   # Mock data for development
├── components/       # React components
│   ├── PhotoCard.tsx      # Individual photo card with hover overlay
│   ├── PhotoGrid.tsx      # Virtualized photo grid
│   ├── Lightbox.tsx       # Full-screen photo viewer
│   ├── UploadZone.tsx     # File upload with drag-drop
│   └── SearchBar.tsx      # Search and filter UI
├── hooks/            # Custom React hooks
│   ├── usePhotos.ts       # Simple photo fetching
│   ├── useInfinitePhotos.ts  # Infinite scroll query
│   ├── useSearch.ts       # Search with filters
│   └── useUpload.ts       # Upload with duplicate detection
├── types/            # TypeScript type definitions
│   └── index.ts
├── utils/            # Utility functions
│   ├── hash.ts       # SHA-256 file hashing
│   └── formatters.ts # Date, file size formatters
├── App.tsx           # Main application component
├── main.tsx          # Application entry point
└── index.css         # Global styles and Tailwind imports
```

## Mock Data vs Real API

The frontend is currently configured to use **mock data** for development. This allows you to build and test the UI before the backend is ready.

### Switch to Real API

1. Open `src/api/client.ts`
2. Change `USE_MOCK_DATA` from `true` to `false`:

```typescript
const USE_MOCK_DATA = false;  // Use real backend API
```

3. Ensure the backend is running on `http://localhost:8080`

### Mock Data Details

- 50 sample photos with random metadata
- 10% duplicate detection rate simulation
- Realistic upload progress simulation
- Images served from picsum.photos (placeholder service)

## Environment Variables

Create a `.env` file in the frontend directory:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Key Features Explained

### Virtualized Rendering

The PhotoGrid component uses `react-virtuoso` to only render photos visible in the viewport. This keeps DOM size constant even with thousands of photos.

### Duplicate Detection

Before uploading, files are hashed using SHA-256 (Web Crypto API):
1. Client calculates file hash
2. Hash is sent to backend to check for duplicates
3. If duplicate exists, upload is skipped
4. If unique, file is uploaded

### React Query Caching

All API requests are cached for 5 minutes with TanStack Query:
- Reduces unnecessary network requests
- Instant navigation back to previously viewed photos
- Automatic background refetching

### Progressive Image Loading

Lightbox displays images in two stages:
1. Preview image (1280px) loads first - fast
2. Original image preloads in background - available on demand

## Keyboard Shortcuts

In Lightbox viewer:
- `←` - Previous photo
- `→` - Next photo
- `E` - Toggle EXIF info panel
- `Esc` - Close lightbox

## Performance Optimizations

1. **Virtual Scrolling**: Only render visible photos
2. **Lazy Loading**: Images load only when in viewport
3. **Query Caching**: 5-minute cache prevents redundant requests
4. **Image Optimization**: Three sizes (thumbnail, preview, original)
5. **Code Splitting**: Vite automatically splits chunks for optimal loading

## Browser Support

- Chrome/Edge 90+
- Firefox 88+
- Safari 14+

## Troubleshooting

### Port already in use

If port 5173 is taken:
```bash
npm run dev -- --port 3000
```

### TypeScript errors

```bash
# Check for type errors
npm run build
```

### API connection issues

1. Verify backend is running on port 8080
2. Check `VITE_API_BASE_URL` in `.env`
3. Ensure proxy is configured in `vite.config.ts`

## Future Enhancements

- [ ] Tag management UI (add/remove/edit)
- [ ] Bulk operations (delete, download)
- [ ] Photo editing (crop, rotate, filters)
- [ ] Album/collection organization
- [ ] Sort options (date, name, size)
- [ ] Geolocation map view
- [ ] Share functionality

## License

MIT
