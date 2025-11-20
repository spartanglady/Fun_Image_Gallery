# Photo Gallery API Documentation

## Base URL
```
http://localhost:8080/api
```

## Overview

This API provides endpoints for managing a local photo gallery, including photo upload, retrieval, search, and tagging functionality. All endpoints return JSON responses unless otherwise specified.

## Authentication

Currently, the API does not require authentication (local-only application). Future versions may implement authentication for multi-user support.

## Error Handling

All error responses follow this format:

```json
{
  "timestamp": "2025-11-19T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/photos/upload"
}
```

### Common HTTP Status Codes

- `200 OK` - Successful GET request
- `201 Created` - Successful POST request (resource created)
- `204 No Content` - Successful DELETE request
- `400 Bad Request` - Invalid request parameters or body
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource (e.g., photo already exists)
- `413 Payload Too Large` - File size exceeds limit
- `415 Unsupported Media Type` - Invalid file type
- `500 Internal Server Error` - Server error (processing failure, etc.)
- `507 Insufficient Storage` - Disk space unavailable

---

## Endpoints

### 1. Upload Photos

Upload one or multiple photos to the gallery.

**Endpoint:** `POST /api/photos/upload`

**Content-Type:** `multipart/form-data`

**Request:**

```http
POST /api/photos/upload HTTP/1.1
Host: localhost:8080
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="files"; filename="photo1.jpg"
Content-Type: image/jpeg

[binary image data]
------WebKitFormBoundary
Content-Disposition: form-data; name="files"; filename="photo2.jpg"
Content-Type: image/jpeg

[binary image data]
------WebKitFormBoundary--
```

**Parameters:**
- `files` (required): One or more image files
  - Supported formats: JPEG, PNG, GIF, WebP, HEIC
  - Max size per file: 50MB (configurable)

**Response:** `201 Created`

```json
{
  "uploaded": 2,
  "failed": 0,
  "photos": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "originalFilename": "photo1.jpg",
      "fileSize": 2457600,
      "mimeType": "image/jpeg",
      "captureDate": "2024-08-15T14:30:00",
      "width": 4032,
      "height": 3024,
      "cameraModel": "Canon EOS R5",
      "iso": "ISO 200",
      "tags": [],
      "uploadedAt": "2025-11-19T10:30:00"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "originalFilename": "photo2.jpg",
      "fileSize": 1843200,
      "mimeType": "image/jpeg",
      "captureDate": "2024-08-15T15:45:00",
      "width": 3840,
      "height": 2160,
      "cameraModel": "Sony A7 IV",
      "iso": "ISO 400",
      "tags": [],
      "uploadedAt": "2025-11-19T10:30:00"
    }
  ]
}
```

**Error Responses:**

- `400 Bad Request` - No files provided or invalid file format
```json
{
  "timestamp": "2025-11-19T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "No files provided in request"
}
```

- `413 Payload Too Large` - File exceeds size limit
```json
{
  "status": 413,
  "message": "File size exceeds maximum allowed size of 50MB"
}
```

- `415 Unsupported Media Type` - Invalid file type
```json
{
  "status": 415,
  "message": "File type 'image/bmp' is not supported. Supported types: JPEG, PNG, GIF, WebP, HEIC"
}
```

- `507 Insufficient Storage` - Not enough disk space
```json
{
  "status": 507,
  "message": "Insufficient disk space available"
}
```

---

### 2. Get Photos (Paginated)

Retrieve a paginated list of all photos.

**Endpoint:** `GET /api/photos`

**Query Parameters:**
- `page` (optional, default: 0): Page number (0-indexed)
- `size` (optional, default: 50): Number of items per page
- `sort` (optional, default: "captureDate,desc"): Sort field and direction

**Request:**

```http
GET /api/photos?page=0&size=50&sort=captureDate,desc HTTP/1.1
Host: localhost:8080
```

**Response:** `200 OK`

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "originalFilename": "sunset.jpg",
      "fileSize": 2457600,
      "mimeType": "image/jpeg",
      "captureDate": "2024-08-15T19:30:00",
      "width": 4032,
      "height": 3024,
      "cameraModel": "Canon EOS R5",
      "iso": "ISO 200",
      "tags": ["sunset", "landscape"],
      "uploadedAt": "2025-11-19T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 247,
  "last": false,
  "first": true,
  "size": 50,
  "number": 0,
  "numberOfElements": 50,
  "empty": false
}
```

---

### 3. Get Photo Image

Stream a photo file in the requested size variant.

**Endpoint:** `GET /api/photos/{id}/image`

**Path Parameters:**
- `id` (required): Photo UUID

**Query Parameters:**
- `type` (optional, default: "preview"): Image variant
  - `original` - Full resolution original
  - `preview` - 1280px preview
  - `thumbnail` - 300px thumbnail

**Request:**

```http
GET /api/photos/550e8400-e29b-41d4-a716-446655440000/image?type=preview HTTP/1.1
Host: localhost:8080
```

**Response:** `200 OK`

```http
HTTP/1.1 200 OK
Content-Type: image/jpeg
Content-Length: 245760
Cache-Control: max-age=86400
Content-Disposition: inline; filename="sunset.jpg"

[binary image data]
```

**Response Headers:**
- `Content-Type`: MIME type of the image
- `Content-Length`: Size of the image in bytes
- `Cache-Control`: Browser caching directive (24 hours)
- `Content-Disposition`: Filename for download

**Error Responses:**

- `404 Not Found` - Photo not found
```json
{
  "status": 404,
  "message": "Photo with id '550e8400-e29b-41d4-a716-446655440000' not found"
}
```

---

### 4. Add Tags to Photo

Add one or more tags to a photo.

**Endpoint:** `POST /api/photos/{id}/tags`

**Path Parameters:**
- `id` (required): Photo UUID

**Request Body:**

```json
{
  "tags": ["sunset", "landscape", "vacation"]
}
```

**Request:**

```http
POST /api/photos/550e8400-e29b-41d4-a716-446655440000/tags HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "tags": ["sunset", "landscape", "vacation"]
}
```

**Response:** `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "sunset.jpg",
  "fileSize": 2457600,
  "mimeType": "image/jpeg",
  "captureDate": "2024-08-15T19:30:00",
  "width": 4032,
  "height": 3024,
  "cameraModel": "Canon EOS R5",
  "iso": "ISO 200",
  "tags": ["sunset", "landscape", "vacation"],
  "uploadedAt": "2025-11-19T10:30:00"
}
```

**Error Responses:**

- `400 Bad Request` - Invalid tag format
```json
{
  "status": 400,
  "message": "Tags cannot be empty or contain special characters"
}
```

- `404 Not Found` - Photo not found
```json
{
  "status": 404,
  "message": "Photo with id '550e8400-e29b-41d4-a716-446655440000' not found"
}
```

---

### 5. Search Photos

Search photos with various filter criteria.

**Endpoint:** `GET /api/photos/search`

**Query Parameters:**
- `tags` (optional): Comma-separated list of tags (AND logic)
- `startDate` (optional): Start of date range (ISO 8601 format)
- `endDate` (optional): End of date range (ISO 8601 format)
- `cameraModel` (optional): Filter by camera model (partial match)
- `query` (optional): Search in original filename (partial match)
- `page` (optional, default: 0): Page number
- `size` (optional, default: 50): Items per page

**Request:**

```http
GET /api/photos/search?tags=sunset,landscape&startDate=2024-08-01&endDate=2024-08-31&page=0&size=20 HTTP/1.1
Host: localhost:8080
```

**Response:** `200 OK`

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "originalFilename": "sunset.jpg",
      "fileSize": 2457600,
      "mimeType": "image/jpeg",
      "captureDate": "2024-08-15T19:30:00",
      "width": 4032,
      "height": 3024,
      "cameraModel": "Canon EOS R5",
      "iso": "ISO 200",
      "tags": ["sunset", "landscape"],
      "uploadedAt": "2025-11-19T10:30:00"
    }
  ],
  "totalPages": 1,
  "totalElements": 12,
  "size": 20,
  "number": 0
}
```

**Example Queries:**

1. Search by tags:
   ```
   GET /api/photos/search?tags=vacation,beach
   ```

2. Search by date range:
   ```
   GET /api/photos/search?startDate=2024-01-01&endDate=2024-12-31
   ```

3. Search by camera model:
   ```
   GET /api/photos/search?cameraModel=Canon
   ```

4. Search by filename:
   ```
   GET /api/photos/search?query=IMG_2024
   ```

5. Combined search:
   ```
   GET /api/photos/search?tags=sunset&cameraModel=Sony&startDate=2024-08-01
   ```

---

### 6. Check Duplicate Photo

Check if a photo with the given file hash already exists.

**Endpoint:** `POST /api/photos/check-duplicate`

**Request Body:**

```json
{
  "fileHash": "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"
}
```

**Request:**

```http
POST /api/photos/check-duplicate HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "fileHash": "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"
}
```

**Response (Duplicate Found):** `409 Conflict`

```json
{
  "duplicate": true,
  "existingPhoto": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "originalFilename": "sunset.jpg",
    "fileSize": 2457600,
    "mimeType": "image/jpeg",
    "captureDate": "2024-08-15T19:30:00",
    "uploadedAt": "2025-11-19T10:30:00"
  },
  "message": "Photo already exists in the gallery"
}
```

**Response (No Duplicate):** `200 OK`

```json
{
  "duplicate": false,
  "message": "No duplicate found, safe to upload"
}
```

**Notes:**
- Use SHA-256 hash algorithm for file hashing
- Client should hash the file before uploading
- If duplicate is found, client can skip upload and show existing photo

---

### 7. Delete Photo

Delete a photo and all its variants.

**Endpoint:** `DELETE /api/photos/{id}`

**Path Parameters:**
- `id` (required): Photo UUID

**Request:**

```http
DELETE /api/photos/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8080
```

**Response:** `204 No Content`

```http
HTTP/1.1 204 No Content
```

**Error Responses:**

- `404 Not Found` - Photo not found
```json
{
  "status": 404,
  "message": "Photo with id '550e8400-e29b-41d4-a716-446655440000' not found"
}
```

**Notes:**
- Deletes the photo record from the database
- Deletes original, preview, and thumbnail files from disk
- Operation is irreversible

---

### 8. Get Photo by ID

Retrieve metadata for a specific photo.

**Endpoint:** `GET /api/photos/{id}`

**Path Parameters:**
- `id` (required): Photo UUID

**Request:**

```http
GET /api/photos/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8080
```

**Response:** `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "sunset.jpg",
  "fileSize": 2457600,
  "mimeType": "image/jpeg",
  "captureDate": "2024-08-15T19:30:00",
  "width": 4032,
  "height": 3024,
  "cameraModel": "Canon EOS R5",
  "iso": "ISO 200",
  "aperture": "f/2.8",
  "shutterSpeed": "1/250",
  "focalLength": "24mm",
  "tags": ["sunset", "landscape"],
  "fileHash": "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3",
  "uploadedAt": "2025-11-19T10:30:00"
}
```

**Error Responses:**

- `404 Not Found` - Photo not found
```json
{
  "status": 404,
  "message": "Photo with id '550e8400-e29b-41d4-a716-446655440000' not found"
}
```

---

## Data Models

### PhotoDTO

Complete photo metadata object returned by the API.

```typescript
interface PhotoDTO {
  id: string;                    // UUID
  originalFilename: string;      // Original uploaded filename
  fileSize: number;              // Size in bytes
  mimeType: string;              // MIME type (e.g., "image/jpeg")
  captureDate: string;           // ISO 8601 datetime
  width: number;                 // Image width in pixels
  height: number;                // Image height in pixels
  cameraModel?: string;          // Camera model from EXIF
  iso?: string;                  // ISO value from EXIF
  aperture?: string;             // Aperture value from EXIF
  shutterSpeed?: string;         // Shutter speed from EXIF
  focalLength?: string;          // Focal length from EXIF
  tags: string[];                // Array of tag strings
  fileHash?: string;             // SHA-256 hash of file
  uploadedAt: string;            // ISO 8601 datetime
}
```

### Page Response

Paginated response wrapper for list endpoints.

```typescript
interface Page<T> {
  content: T[];                  // Array of items
  pageable: {
    pageNumber: number;          // Current page (0-indexed)
    pageSize: number;            // Items per page
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;              // Offset from start
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;            // Total number of pages
  totalElements: number;         // Total number of items
  last: boolean;                 // Is this the last page?
  first: boolean;                // Is this the first page?
  size: number;                  // Page size
  number: number;                // Current page number
  numberOfElements: number;      // Number of items in this page
  empty: boolean;                // Is the page empty?
}
```

---

## Rate Limiting

Currently, no rate limiting is implemented (local-only application). Future versions may implement rate limiting for upload endpoints to prevent resource exhaustion.

---

## Best Practices

### 1. Image Retrieval
- Use `thumbnail` variant for grid displays
- Use `preview` variant for lightbox/modal views
- Only load `original` when user explicitly requests download or full-size view

### 2. Pagination
- Use recommended page size of 50 for optimal performance
- Implement infinite scroll on the frontend using incremental page loads

### 3. Caching
- Browser will cache image responses for 24 hours (Cache-Control header)
- Implement client-side caching with React Query for API responses

### 4. Error Handling
- Always check for error status codes
- Display user-friendly error messages
- Implement retry logic for transient failures (500 errors)

### 5. File Upload
- Validate file type and size on client before upload
- Use duplicate check endpoint before uploading to save bandwidth
- Show upload progress to users
- Support batch uploads for better UX

---

## CORS Configuration

The API is configured to accept requests from:
- `http://localhost:5173` (Vite development server)

All CORS headers are automatically included in responses. See [cors-config.md](cors-config.md) for detailed configuration.

---

## Future Endpoints (Post-MVP)

Planned endpoints for future releases:

- `POST /api/albums` - Create photo album
- `GET /api/albums` - List albums
- `POST /api/albums/{id}/photos` - Add photos to album
- `POST /api/photos/bulk-delete` - Delete multiple photos
- `GET /api/photos/export` - Export photos as ZIP
- `PUT /api/photos/{id}` - Update photo metadata
- `POST /api/photos/{id}/rotate` - Rotate photo

---

## Changelog

### Version 1.0.0 (Planned)
- Initial API implementation
- Photo upload, retrieval, search, and tagging
- Duplicate detection
- EXIF metadata extraction
