# Fun Image Gallery - Project Context for Claude

## Project Overview

This is a **local high-performance photo gallery application** with a React frontend and Spring Boot backend. The primary focus is on performance optimization, responsive UI, and efficient handling of large photo collections.

**Status:** Currently in planning phase. Specification complete, no code implemented yet.

**Constraints:**
- Local hosting only (no cloud deployment)
- Photos only (no video support)
- H2 in-memory database with file-based persistence

## Architecture

### Backend (Spring Boot 3.x)
- **Language:** Java 17+
- **Build Tool:** Maven or Gradle (to be decided during setup)
- **Database:** H2 (in-memory with file persistence)
- **ORM:** Spring Data JPA
- **Image Processing:** Thumbnailator library (`net.coobird:thumbnailator`)
- **Metadata Extraction:** metadata-extractor (`com.drewnoakes:metadata-extractor`)
- **Storage:** Local filesystem with organized directory structure

### Frontend (React + TypeScript)
- **Build Tool:** Vite (for fast HMR)
- **Language:** TypeScript with strict typing
- **Styling:** Tailwind CSS
- **State Management:** TanStack Query (React Query v5)
- **Virtualization:** react-virtuoso or react-window
- **File Upload:** react-dropzone

## Core Features

### MVP Features
1. Multi-file drag-and-drop photo upload
2. Automatic thumbnail (300px) and preview (1280px) generation
3. EXIF metadata extraction (camera model, ISO, capture date, dimensions)
4. Virtualized photo grid for large collections
5. Lightbox viewer with progressive image loading
6. Photo tagging system
7. Pagination support

### Enhanced Features (Beyond MVP)
1. **Duplicate Detection:** SHA-256 hash-based duplicate checking before upload
2. **Advanced Search:**
   - Filter by tags (multi-select)
   - Date range filtering
   - Camera model filtering
   - Filename text search
   - Combined filter criteria
3. **Error Handling:**
   - File validation (type, size, corruption)
   - Disk space checks
   - Transaction rollbacks on failures
   - User-friendly error messages

### Future Enhancements (Post-MVP)
- Albums/Collections
- Bulk operations
- Export functionality (ZIP downloads)
- Photo editing (crop, rotate, filters)
- Geolocation display (GPS EXIF data)

## File Structure

### Storage Organization
```
./uploads/
  original/{yyyy}/{mm}/{uuid}.jpg
  thumbnails/{uuid}.jpg
  previews/{uuid}.jpg
```

**Rationale:** Date-based folders for originals prevent single-folder bottlenecks with 10k+ files.

### Metadata Storage
- Primary metadata stored in H2 database (`Photo` entity)
- Consider `.metadata.json` files alongside images for user-friendly filesystem browsing

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/photos/upload` | Multipart file upload with processing pipeline |
| GET | `/api/photos` | Paginated photo list (returns `Page<PhotoDTO>`) |
| GET | `/api/photos/{id}/image?type=` | Stream image (original/preview/thumbnail) |
| POST | `/api/photos/{id}/tags` | Add tags to a photo |
| GET | `/api/photos/search` | Advanced search with filters |
| POST | `/api/photos/check-duplicate` | Check if file hash already exists |

## Data Model

### Photo Entity Fields
- `id` (UUID or Long)
- `originalFilename` (String)
- `storedPath` (String - relative path)
- `fileSize` (Long)
- `mimeType` (String)
- `captureDate` (LocalDateTime)
- `width`, `height` (Int)
- `cameraModel` (String - EXIF)
- `iso` (String - EXIF)
- `tags` (Set<String>)
- `fileHash` (String - SHA-256 for duplicate detection)

## Performance Optimizations

### Backend
- Browser caching with `Cache-Control: max-age=86400` headers
- Efficient byte streaming for image delivery
- Pagination for all list endpoints
- Organized file storage to avoid folder bottlenecks

### Frontend
- Virtual scrolling (only render visible images)
- Lazy loading with `loading="lazy"` attribute
- React Query caching with 5-minute stale time
- Progressive image loading (preview first, then original)
- Infinite scroll with `useInfiniteQuery`

## Development Workflow

### Implementation Order (from Project_Specification.md Section 6)
1. Setup Spring Boot backend with core services
2. Implement image processing pipeline
3. Add error handling and validation
4. Build REST API layer
5. Implement search and duplicate detection
6. Setup Vite + React + TypeScript frontend
7. Build API client layer
8. Implement virtualized photo grid
9. Build drag-and-drop upload with duplicate detection
10. Add search UI components

## Important Design Decisions

### Why H2?
- Lightweight for local development
- File-based persistence for data retention
- Suitable for <10k photos
- **Migration Path:** Monitor performance; migrate to SQLite or PostgreSQL if needed at >5k photos

### Why Virtualization?
- DOM node count becomes problematic with >1000 images
- Virtual scrolling keeps DOM size constant regardless of gallery size
- Critical for smooth scrolling experience

### Why Three Image Sizes?
- **Thumbnail (300px):** Grid display, minimal bandwidth
- **Preview (1280px):** Lightbox view, fast loading
- **Original:** Available for download/viewing, loaded on-demand

### Why Client-Side Hashing?
- Prevents redundant uploads early in the pipeline
- Saves server processing time and storage space
- Better user experience (instant duplicate detection)

## Error Handling Philosophy

All operations should fail gracefully with:
1. **Clear error messages** to users
2. **Rollback mechanisms** for incomplete operations
3. **Logging** for debugging
4. **Retry logic** for transient failures
5. **Validation** before processing (fail fast)

## Code Style Preferences

### Backend (Java/Spring Boot)
- Follow Spring Boot best practices
- Use constructor injection over field injection
- Keep controllers thin (delegate to services)
- Use DTOs for API responses (never expose entities directly)
- Comprehensive JavaDoc for public APIs

### Frontend (React/TypeScript)
- Strict TypeScript mode enabled
- Functional components with hooks
- Smart/dumb component separation
- Custom hooks for reusable logic
- Tailwind utility classes (avoid custom CSS when possible)

## Testing Strategy

### Backend
- Unit tests for services (image processing, storage)
- Integration tests for API endpoints
- Test error handling paths thoroughly

### Frontend
- Component tests for UI components
- Integration tests for user flows
- E2E tests for critical paths (upload, view, search)

## Key Files to Reference

- **Project_Specification.md:** Complete technical specification with implementation details
- **claude.md:** This file - project context and conventions

## Common Tasks

### When Setting Up Backend
- Initialize Spring Boot with Web, JPA, H2 dependencies
- Configure H2 file-based persistence in `application.properties`
- Add Thumbnailator and metadata-extractor dependencies
- Create storage directory structure
- Configure max file upload size

### When Setting Up Frontend
- Initialize Vite with React + TypeScript template
- Install: tailwindcss, @tanstack/react-query, react-virtuoso, react-dropzone
- Configure Tailwind CSS
- Setup API base URL as environment variable
- Configure React Query defaults (staleTime: 5 minutes)

### When Adding New Features
- Update Photo entity if new metadata needed
- Add corresponding DTO fields
- Update PhotoService and Controller
- Add frontend API client method
- Update UI components
- Update this claude.md if architectural changes

## Troubleshooting Common Issues

### H2 Database Locked
- Ensure only one application instance is running
- Check for stale lock files in H2 data directory

### Image Processing Fails
- Verify Thumbnailator dependency is correct version
- Check for corrupted image files
- Validate supported MIME types

### Large Upload Failures
- Check Spring Boot `spring.servlet.multipart.max-file-size` property
- Verify disk space availability
- Check client timeout settings

## Questions to Ask User Before Implementation

1. **Build Tool Preference:** Maven or Gradle?
2. **Photo Size Limits:** What's the max file size per photo?
3. **Storage Location:** Where should the `./uploads` directory be created?
4. **Port Configuration:** Default ports for frontend (5173) and backend (8080)?
5. **Virtual Scroller:** Preference between react-virtuoso vs react-window?

## Future Considerations

- **Database Migration:** Monitor H2 performance; plan migration path if gallery exceeds 5k photos
- **Cloud Storage:** If requirements change, consider AWS S3 or similar (requires architecture update)
- **Video Support:** Would require different processing pipeline and player component
- **Mobile App:** Consider separating API into standalone service if mobile app is needed
- **Multi-user Support:** Would require authentication, authorization, and user-specific galleries

---

**Last Updated:** 2025-11-19
**Project Phase:** Planning/Specification Complete
**Next Steps:** Begin backend setup (Spring Boot initialization)
