# Fun Image Gallery

A high-performance, local photo gallery application built with React and Spring Boot. Optimized for handling large photo collections with efficient image rendering, responsive UI, and smart caching.

## Tech Stack

### Backend
- **Framework:** Spring Boot 3.x (Java 17+)
- **Build Tool:** Gradle
- **Database:** H2 (in-memory with file-based persistence)
- **ORM:** Spring Data JPA
- **Image Processing:** Thumbnailator (`net.coobird:thumbnailator`)
- **Metadata Extraction:** Metadata Extractor (`com.drewnoakes:metadata-extractor`)
- **Storage:** Local filesystem

### Frontend
- **Framework:** React 18+ with TypeScript
- **Build Tool:** Vite
- **Styling:** Tailwind CSS
- **State Management:** TanStack Query (React Query v5)
- **Virtualization:** react-virtuoso
- **File Upload:** react-dropzone

## Features

### Core Features
- Multi-file drag-and-drop photo upload
- Automatic thumbnail (300px) and preview (1280px) generation
- EXIF metadata extraction (camera model, ISO, capture date, dimensions)
- Virtualized photo grid for large collections
- Lightbox viewer with progressive image loading
- Photo tagging system
- Pagination support

### Advanced Features
- SHA-256 hash-based duplicate detection
- Advanced search and filtering:
  - Filter by tags (multi-select)
  - Date range filtering
  - Camera model filtering
  - Filename text search
- Comprehensive error handling with rollback mechanisms
- Browser caching for optimal performance

## Project Structure

```
Fun_Image_Gallery/
├── backend/                 # Spring Boot application
│   ├── src/
│   ├── build.gradle
│   └── .env.example
├── frontend/                # React application
│   ├── src/
│   ├── package.json
│   └── .env.example
├── uploads/                 # Photo storage (gitignored)
│   ├── original/           # Original photos organized by date
│   ├── thumbnails/         # 300px thumbnails
│   └── previews/           # 1280px previews
├── docs/                    # Documentation
│   ├── API.md              # API endpoint documentation
│   └── cors-config.md      # CORS configuration guide
└── README.md
```

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Node.js 18+ and npm
- Git

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

3. Edit `.env` to configure storage paths and database settings if needed.

4. Run the application:
   ```bash
   ./gradlew bootRun
   ```

   The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

4. Start the development server:
   ```bash
   npm run dev
   ```

   The frontend will start on `http://localhost:5173`

## Running the Application

1. Start the backend server (from `backend/` directory):
   ```bash
   ./gradlew bootRun
   ```

2. Start the frontend development server (from `frontend/` directory):
   ```bash
   npm run dev
   ```

3. Open your browser and navigate to `http://localhost:5173`

## API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/photos/upload` | Upload photos (multipart/form-data) |
| GET | `/api/photos` | Get paginated list of photos |
| GET | `/api/photos/{id}/image?type=` | Stream image (original/preview/thumbnail) |
| POST | `/api/photos/{id}/tags` | Add tags to a photo |
| GET | `/api/photos/search` | Search photos with filters |
| POST | `/api/photos/check-duplicate` | Check if file hash exists |
| DELETE | `/api/photos/{id}` | Delete a photo |

For detailed API documentation, see [docs/API.md](docs/API.md)

## Storage Organization

Photos are organized to prevent filesystem bottlenecks with large collections:

- **Original Photos:** `uploads/original/{yyyy}/{mm}/{uuid}.jpg`
- **Thumbnails:** `uploads/thumbnails/{uuid}.jpg`
- **Previews:** `uploads/previews/{uuid}.jpg`

## Performance Optimizations

### Backend
- Browser caching with `Cache-Control` headers (24-hour max-age)
- Efficient byte streaming for image delivery
- Pagination for all list endpoints
- Date-based folder organization to avoid single-folder bottlenecks

### Frontend
- Virtual scrolling (only render visible images)
- Lazy loading with native `loading="lazy"` attribute
- React Query caching with 5-minute stale time
- Progressive image loading (preview first, original on-demand)
- Infinite scroll for seamless browsing

## Database

The application uses H2 in-memory database with file-based persistence:

- **Location:** `./data/` directory (gitignored)
- **Suitable for:** Collections up to ~10,000 photos
- **Migration Path:** For larger collections, consider migrating to SQLite or PostgreSQL

## Error Handling

The application implements comprehensive error handling:

- File validation (type, size, corruption checks)
- Disk space checks before upload
- Transaction rollbacks on processing failures
- User-friendly error messages
- Retry mechanisms for transient failures

## Development

### Backend Development
- Spring Boot auto-reloads on code changes (with Spring DevTools)
- H2 console available at `http://localhost:8080/h2-console`
- API testing with tools like Postman or curl

### Frontend Development
- Vite provides fast HMR (Hot Module Replacement)
- TypeScript strict mode enabled
- Tailwind CSS for styling
- React DevTools recommended for debugging

## Testing

### Backend Tests
```bash
cd backend
./gradlew test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Building for Production

### Backend
```bash
cd backend
./gradlew build
java -jar build/libs/photo-gallery-*.jar
```

### Frontend
```bash
cd frontend
npm run build
# Serve the 'dist' folder with a static server
```

## Troubleshooting

### H2 Database Locked
- Ensure only one backend instance is running
- Delete stale lock files in the `data/` directory

### Large Upload Failures
- Check backend max file size setting in `application.properties`
- Verify available disk space
- Check client timeout settings

### CORS Issues
- Ensure backend CORS configuration allows `http://localhost:5173`
- See [docs/cors-config.md](docs/cors-config.md) for details

## Documentation

- [API Documentation](docs/API.md) - Detailed API endpoint reference
- [CORS Configuration](docs/cors-config.md) - CORS setup guide
- [Project Specification](Project_Specification.md) - Complete technical specification

## Future Enhancements

- Albums/Collections grouping
- Bulk operations (delete, tag multiple photos)
- Export functionality (ZIP download)
- Sort options (date, name, size, camera model)
- Photo editing (crop, rotate, filters)
- Geolocation display (GPS EXIF data)

## License

This is a local development project for personal use.

## Support

For issues or questions, refer to the documentation in the `docs/` directory or the project specification.
