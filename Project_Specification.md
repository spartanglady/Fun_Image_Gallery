# Project Specification: Local High-Performance Photo Gallery

## 1\. Project Overview

**Goal:** Build a "Photo Gallery MVP" consisting of a React frontend and Spring Boot backend.
**Primary Focus:** High performance, responsive UI, optimized data transfer, and efficient image rendering.
**Constraint:** Local hosting only. H2 In-memory database. Photos only (no video).

## 2\. Tech Stack & Architecture

### Backend (Spring Boot)

  * **Framework:** Spring Boot 3.x (Java 17+)
  * **Build Tool:** Maven or Gradle
  * **Database:** H2 (In-Memory, File-based persistence enabled for dev)
  * **ORM:** Spring Data JPA
  * **Image Processing:** `net.coobird:thumbnailator` (High-quality resizing)
  * **Metadata Extraction:** `com.drewnoakes:metadata-extractor` (EXIF data)
  * **Storage:** Local Filesystem (Configurable root directory)

### Frontend (React)

  * **Build Tool:** Vite (Fast HMR)
  * **Language:** TypeScript (Strict typing preferred)
  * **Styling:** Tailwind CSS
  * **State/Data Fetching:** TanStack Query (React Query v5) - **Critical for performance caching**
  * **Virtualization:** `react-virtuoso` or `react-window` (For handling large grids DOM-efficiently)
  * **File Upload:** `react-dropzone`

-----

## 3\. Backend Implementation Details

### A. Domain Model (`Photo` Entity)

The entity must store metadata to avoid reading files on every request.

  * `id` (UUID or Long)
  * `originalFilename` (String)
  * `storedPath` (String - relative path in storage)
  * `fileSize` (Long)
  * `mimeType` (String)
  * `captureDate` (LocalDateTime - extracted from EXIF or file creation)
  * `width` (Int)
  * `height` (Int)
  * `cameraModel` (String - from EXIF)
  * `iso` (String - from EXIF)
  * `tags` (ElementCollection or Set\<String\>)

### B. API Endpoints

  * `POST /api/photos/upload`: Multipart file upload.
      * **Process:** Save original -\> Extract EXIF -\> Generate "Thumbnail" (300px) -\> Generate "Preview" (1280px) -\> Save DB Entry.
  * `GET /api/photos`: Returns `Page<PhotoDTO>`.
      * **Optimization:** Must support pagination (e.g., `?page=0&size=50`).
  * `GET /api/photos/{id}/image`: Streaming endpoint.
      * Query Param: `?type=` (`original` | `preview` | `thumbnail`).
      * **Header:** Must set `Cache-Control: max-age=86400` to leverage browser caching.
  * `POST /api/photos/{id}/tags`: Add tags to a photo.
  * `GET /api/photos/search`: Advanced search endpoint.
      * Query Params: `?tags=`, `?startDate=`, `?endDate=`, `?cameraModel=`, `?query=` (filename search)
      * Returns: `Page<PhotoDTO>` matching criteria
  * `POST /api/photos/check-duplicate`: Upload duplicate detection.
      * Accept file hash (SHA-256) from client
      * Return existing photo ID if hash matches, preventing redundant uploads

### C. Storage Structure

On the local disk (e.g., `./uploads`), structure files to avoid a single folder containing 10k files:

  * `./uploads/original/{yyyy}/{mm}/{uuid}.jpg`
  * `./uploads/thumbnails/{uuid}.jpg`
  * `./uploads/previews/{uuid}.jpg`

**Note on Filesystem Browsing:** While UUIDs prevent naming collisions, consider storing the original filename in a `.metadata.json` file alongside images or as a separate mapping file for user-friendly browsing if users access the upload directory directly.

### D. Error Handling Strategy

Robust error handling is critical for production readiness:

  * **Upload Failures:**
      * Validate file types before processing (reject non-image files)
      * Implement file size limits (e.g., max 50MB per photo)
      * Handle corrupted/malformed images gracefully with clear error messages
      * Implement disk space checks before accepting uploads
      * Rollback: If thumbnail/preview generation fails, delete the original and return error
  * **Storage Issues:**
      * Check disk space availability before accepting uploads
      * Implement retry logic for transient filesystem errors
      * Log all storage failures for debugging
  * **Database Constraints:**
      * Handle duplicate UUID collisions (though extremely unlikely)
      * Implement transaction boundaries for photo upload pipeline
  * **Client-side:**
      * Display user-friendly error messages for failed uploads
      * Implement upload retry mechanism with exponential backoff
      * Show disk space warnings if quota exceeded

### E. Database Migration Considerations

**Current Choice: H2 In-Memory Database**
  * Suitable for initial development and small galleries (<10k photos)
  * File-based persistence enabled for data retention between restarts

**Migration Path (Future):**
  * If gallery grows large or performance degrades under load, migrate to:
      * **SQLite:** Single-file, serverless, better for read-heavy workloads
      * **PostgreSQL:** Full-featured RDBMS for advanced querying and concurrency
  * JPA abstraction makes this migration straightforward
  * Recommendation: Monitor H2 performance with >5000 photos and consider migration if needed

-----

## 4\. Frontend Implementation Details

### A. Components

1.  **PhotoGrid (Smart Component):**
      * Use `react-virtuoso` Grid.
      * Fetch data using `useInfiniteQuery` (React Query) to handle pagination seamlessly as the user scrolls.
      * **Performance Rule:** Only render images currently in the viewport.
2.  **PhotoCard (Dumb Component):**
      * Display the `thumbnail` version of the image.
      * Show overlay on hover (File size, Date).
3.  **Lightbox / Viewer:**
      * Modal that opens when a photo is clicked.
      * Initially load the `preview` image (fast load).
      * Optionally load `original` in background.
      * Display EXIF data (ISO, Camera, Date) in a side panel.
4.  **UploadZone:**
      * Floating Action Button (FAB) or dedicated area.
      * Support multi-file drag & drop.
      * Show progress bars for uploads.
      * Implement duplicate detection (hash files client-side before upload)
5.  **SearchBar (Smart Component):**
      * Filter by tags (multi-select dropdown)
      * Date range picker (capture date)
      * Camera model filter
      * Filename search input
      * Clear/reset filters button

### B. Optimization Strategies (Requirements)

  * **Image Loading:** Use the `loading="lazy"` attribute on `<img>` tags.
  * **Blur-up:** (Optional) Use a CSS background color based on dominant color while loading.
  * **Caching:** Configure React Query `staleTime` to 5 minutes to prevent refetching grid data when navigating back from the Lightbox.

-----

## 5\. Additional Features & Enhancements

### A. Duplicate Detection System

Prevent users from accidentally uploading the same photo multiple times:

  * **Client-side:** Calculate SHA-256 hash of file before upload
  * **Server-side:** Store file hash in `Photo` entity
  * **Process:**
      1. Client uploads file hash first via `POST /api/photos/check-duplicate`
      2. Server checks if hash exists in database
      3. If duplicate found, return existing photo details to client
      4. If unique, proceed with full upload
  * **Benefits:** Saves storage space and processing time

### B. Search & Filtering Capabilities

Beyond basic tag filtering, implement comprehensive search:

  * **By Tags:** Multi-tag AND/OR filtering
  * **By Date Range:** Filter photos by capture date (e.g., "Summer 2024")
  * **By Camera Model:** Useful for photographers with multiple cameras
  * **By Filename:** Text search on original filenames
  * **Combined Filters:** Allow multiple filter criteria simultaneously
  * **UI:** Dedicated search bar with filter chips showing active filters

### C. Future Enhancements (Post-MVP)

  * Albums/Collections grouping
  * Bulk operations (delete, tag multiple photos)
  * Export functionality (ZIP download of selected photos)
  * Sort options (date, name, size, camera model)
  * Photo editing (crop, rotate, filters)
  * Geolocation display (if GPS data available in EXIF)

-----

## 6\. Step-by-Step Instructions for the Agent

1.  **Setup Backend:** Initialize Spring Boot with Web, JPA, H2. Create the `FileStorageService` to handle reading/writing to disk.
2.  **Implement Processing:** Create the `ImageProcessingService` using Thumbnailator and Metadata-Extractor.
3.  **Add Error Handling:** Implement validation, disk space checks, and rollback mechanisms.
4.  **API Layer:** Build the Controller. Ensure the Image retrieval endpoint writes bytes efficiently to the response stream.
5.  **Implement Search:** Add search endpoint with filtering by tags, date, camera model, and filename.
6.  **Duplicate Detection:** Implement hash-based duplicate checking on upload.
7.  **Setup Frontend:** Initialize Vite + React + TS. Install Tailwind.
8.  **Connect:** Build the API client (using `axios` or `fetch`).
9.  **Build Grid:** Implement the virtualized grid connected to the Paginated API.
10. **Build Upload:** Implement the Drag & Drop upload with duplicate detection.
11. **Add Search UI:** Implement SearchBar component with filtering capabilities.

-----

