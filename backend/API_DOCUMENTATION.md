# Photo Gallery API Documentation

Base URL: `http://localhost:8080`

## Table of Contents

1. [Upload Photo](#upload-photo)
2. [Get All Photos](#get-all-photos)
3. [Get Photo Details](#get-photo-details)
4. [Get Photo Image](#get-photo-image)
5. [Add Tags to Photo](#add-tags-to-photo)
6. [Delete Photo](#delete-photo)
7. [Check Duplicate](#check-duplicate)
8. [Search Photos](#search-photos)
9. [Error Responses](#error-responses)

---

## Upload Photo

Upload a new photo with automatic thumbnail and preview generation.

**Endpoint:** `POST /api/photos/upload`

**Request:**
- Content-Type: `multipart/form-data`
- Body Parameters:
  - `file` (required): The image file
  - `tags` (optional): Comma-separated tags (e.g., "nature,landscape,sunset")

**Success Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "sunset.jpg",
  "fileSize": 2048576,
  "message": "Photo uploaded successfully",
  "success": true
}
```

**Duplicate Response (409 Conflict):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Duplicate photo detected",
  "success": false
}
```

**curl Example:**
```bash
curl -X POST http://localhost:8080/api/photos/upload \
  -F "file=@/path/to/photo.jpg" \
  -F "tags=nature,landscape"
```

**Processing Pipeline:**
1. File validation (type, size, corruption)
2. SHA-256 hash calculation for duplicate detection
3. EXIF metadata extraction (camera, ISO, aperture, etc.)
4. Original file storage (organized by year/month)
5. Thumbnail generation (300px max dimension)
6. Preview generation (1280px max dimension)
7. Database entry creation

**Validation Rules:**
- Max file size: 50MB
- Allowed types: JPEG, PNG, GIF, WebP, BMP
- File must be a valid, non-corrupted image

---

## Get All Photos

Retrieve a paginated list of all photos with metadata.

**Endpoint:** `GET /api/photos`

**Query Parameters:**
- `page` (optional, default: 0): Page number (0-indexed)
- `size` (optional, default: 50): Number of items per page
- `sortBy` (optional, default: "captureDate"): Field to sort by
- `sortDir` (optional, default: "desc"): Sort direction ("asc" or "desc")

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "originalFilename": "sunset.jpg",
      "fileSize": 2048576,
      "mimeType": "image/jpeg",
      "captureDate": "2024-06-15T18:30:45",
      "width": 3840,
      "height": 2160,
      "cameraModel": "Canon EOS R5",
      "iso": "100",
      "aperture": "f/8.0",
      "shutterSpeed": "1/125s",
      "focalLength": 24,
      "tags": ["nature", "landscape", "sunset"],
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 150,
  "totalPages": 3,
  "last": false,
  "first": true,
  "numberOfElements": 50
}
```

**curl Examples:**
```bash
# First page with default settings
curl http://localhost:8080/api/photos

# Page 2 with 20 items, sorted by filename ascending
curl "http://localhost:8080/api/photos?page=1&size=20&sortBy=originalFilename&sortDir=asc"

# Sorted by creation date, newest first
curl "http://localhost:8080/api/photos?sortBy=createdAt&sortDir=desc"
```

**Sortable Fields:**
- `captureDate` (default)
- `originalFilename`
- `fileSize`
- `createdAt`
- `updatedAt`
- `cameraModel`
- `width`
- `height`

---

## Get Photo Details

Retrieve detailed metadata for a specific photo.

**Endpoint:** `GET /api/photos/{id}`

**Path Parameters:**
- `id` (required): Photo UUID

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "sunset.jpg",
  "fileSize": 2048576,
  "mimeType": "image/jpeg",
  "captureDate": "2024-06-15T18:30:45",
  "width": 3840,
  "height": 2160,
  "cameraModel": "Canon EOS R5",
  "iso": "100",
  "aperture": "f/8.0",
  "shutterSpeed": "1/125s",
  "focalLength": 24,
  "tags": ["nature", "landscape"],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**curl Example:**
```bash
curl http://localhost:8080/api/photos/550e8400-e29b-41d4-a716-446655440000
```

---

## Get Photo Image

Stream the actual image file (original, preview, or thumbnail).

**Endpoint:** `GET /api/photos/{id}/image`

**Path Parameters:**
- `id` (required): Photo UUID

**Query Parameters:**
- `type` (optional, default: "thumbnail"): Image type
  - `original`: Full resolution image
  - `preview`: 1280px max dimension
  - `thumbnail`: 300px max dimension

**Response (200 OK):**
- Binary image data
- Headers:
  - `Content-Type`: Original MIME type (e.g., "image/jpeg")
  - `Cache-Control`: "max-age=86400, public" (1 day)
  - `Content-Length`: Image size in bytes

**curl Examples:**
```bash
# Download thumbnail
curl http://localhost:8080/api/photos/{id}/image?type=thumbnail \
  -o thumbnail.jpg

# Download preview
curl http://localhost:8080/api/photos/{id}/image?type=preview \
  -o preview.jpg

# Download original
curl http://localhost:8080/api/photos/{id}/image?type=original \
  -o original.jpg
```

**HTML Usage:**
```html
<!-- Thumbnail in grid -->
<img src="http://localhost:8080/api/photos/{id}/image?type=thumbnail"
     alt="Photo"
     loading="lazy">

<!-- Preview in lightbox -->
<img src="http://localhost:8080/api/photos/{id}/image?type=preview"
     alt="Photo">
```

**Performance Notes:**
- Images are cached by the browser for 1 day
- Efficient byte streaming (no memory buffering)
- Thumbnails load ~10x faster than previews
- Previews load ~5x faster than originals

---

## Add Tags to Photo

Add one or more tags to an existing photo.

**Endpoint:** `POST /api/photos/{id}/tags`

**Path Parameters:**
- `id` (required): Photo UUID

**Request:**
- Content-Type: `application/json`
- Body:
```json
{
  "tags": ["sunset", "beach", "vacation"]
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "sunset.jpg",
  "tags": ["nature", "landscape", "sunset", "beach", "vacation"],
  ...
}
```

**curl Example:**
```bash
curl -X POST http://localhost:8080/api/photos/{id}/tags \
  -H "Content-Type: application/json" \
  -d '{"tags": ["sunset", "beach", "vacation"]}'
```

**Notes:**
- Tags are automatically converted to lowercase
- Duplicate tags are ignored
- Tags are trimmed of whitespace

---

## Delete Photo

Delete a photo and all associated files (original, preview, thumbnail).

**Endpoint:** `DELETE /api/photos/{id}`

**Path Parameters:**
- `id` (required): Photo UUID

**Response (204 No Content):**
- Empty body

**curl Example:**
```bash
curl -X DELETE http://localhost:8080/api/photos/{id}
```

**Cascade Deletion:**
1. Original file from `uploads/original/{year}/{month}/{uuid}.jpg`
2. Thumbnail from `uploads/thumbnails/{uuid}.jpg`
3. Preview from `uploads/previews/{uuid}.jpg`
4. Database entry

---

## Check Duplicate

Check if a photo with a given SHA-256 hash already exists.

**Endpoint:** `POST /api/photos/check-duplicate`

**Request:**
- Content-Type: `application/json`
- Body:
```json
{
  "fileHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
}
```

**Response (200 OK) - Duplicate Found:**
```json
{
  "isDuplicate": true,
  "existingPhotoId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Photo already exists"
}
```

**Response (200 OK) - Unique:**
```json
{
  "isDuplicate": false,
  "existingPhotoId": null,
  "message": "Photo is unique"
}
```

**curl Example:**
```bash
curl -X POST http://localhost:8080/api/photos/check-duplicate \
  -H "Content-Type: application/json" \
  -d '{"fileHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"}'
```

**Client-Side Hash Calculation (JavaScript):**
```javascript
async function calculateSHA256(file) {
  const arrayBuffer = await file.arrayBuffer();
  const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  return hashHex;
}

// Usage
const file = document.getElementById('fileInput').files[0];
const hash = await calculateSHA256(file);
const response = await fetch('/api/photos/check-duplicate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ fileHash: hash })
});
```

---

## Search Photos

Advanced search with multiple filter criteria.

**Endpoint:** `GET /api/photos/search`

**Query Parameters:**
- `tags` (optional): Comma-separated tags (e.g., "nature,landscape")
- `startDate` (optional): Start of capture date range (ISO 8601 format)
- `endDate` (optional): End of capture date range (ISO 8601 format)
- `cameraModel` (optional): Camera model (partial match, case-insensitive)
- `query` (optional): Filename search (partial match, case-insensitive)
- `page` (optional, default: 0): Page number
- `size` (optional, default: 50): Page size
- `sortBy` (optional, default: "captureDate"): Sort field
- `sortDir` (optional, default: "desc"): Sort direction

**Response (200 OK):**
Same structure as "Get All Photos" endpoint.

**curl Examples:**
```bash
# Search by tags
curl "http://localhost:8080/api/photos/search?tags=nature,landscape"

# Search by date range
curl "http://localhost:8080/api/photos/search?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59"

# Search by camera model
curl "http://localhost:8080/api/photos/search?cameraModel=Canon"

# Search by filename
curl "http://localhost:8080/api/photos/search?query=sunset"

# Combined search
curl "http://localhost:8080/api/photos/search?tags=nature&cameraModel=Canon&startDate=2024-06-01T00:00:00"
```

**Search Logic:**
- All criteria are combined with AND logic
- Tag search: Photo must have at least one of the specified tags
- Date range: Inclusive of start and end dates
- Camera model: Partial, case-insensitive match
- Filename: Partial, case-insensitive match

**Date Format:**
ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`

Examples:
- `2024-01-15T00:00:00` (midnight on Jan 15, 2024)
- `2024-06-30T23:59:59` (end of day on Jun 30, 2024)

---

## Error Responses

All error responses follow this structure:

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "status": 400,
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/photos/upload"
}
```

### Common Error Codes

**400 Bad Request**
- Invalid file type
- File corruption
- Invalid arguments
- Missing required parameters

Example:
```json
{
  "error": "Invalid File",
  "message": "Invalid file type. Allowed types: JPEG, PNG, GIF, WebP, BMP",
  "status": 400,
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/photos/upload"
}
```

**404 Not Found**
- Photo does not exist

Example:
```json
{
  "error": "Photo Not Found",
  "message": "Photo not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "status": 404,
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/photos/550e8400-e29b-41d4-a716-446655440000"
}
```

**409 Conflict**
- Duplicate photo detected

Example:
```json
{
  "error": "Duplicate Photo",
  "message": "Duplicate photo detected. Photo already exists with id: 550e8400-e29b-41d4-a716-446655440000",
  "status": 409,
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/photos/upload"
}
```

**413 Payload Too Large**
- File size exceeds 50MB

Example:
```json
{
  "error": "File Too Large",
  "message": "File size exceeds maximum allowed size of 50MB",
  "status": 413,
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/photos/upload"
}
```

**500 Internal Server Error**
- Storage failures
- Image processing errors
- Unexpected errors

Example:
```json
{
  "error": "Storage Error",
  "message": "Failed to store file: Disk space insufficient",
  "status": 500,
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/photos/upload"
}
```

---

## Rate Limits

Currently no rate limits are enforced (local deployment only).

## Authentication

Currently no authentication is required (MVP version).

## CORS

The API accepts requests from:
- `http://localhost:5173` (default Vite dev server)
- `http://localhost:3000` (alternative dev server)

To add more origins, edit `cors.allowed-origins` in `application.properties`.

## Performance Best Practices

1. **Use Thumbnails for Grids:** Always use `type=thumbnail` for photo grids
2. **Use Previews for Lightboxes:** Use `type=preview` for modal views
3. **Only Load Originals On-Demand:** Download originals only when explicitly requested
4. **Leverage Browser Caching:** Images are cached for 24 hours
5. **Paginate Results:** Use reasonable page sizes (20-50 items)
6. **Pre-check Duplicates:** Calculate hash client-side before uploading

## WebSocket/Real-time Updates

Not currently supported. Poll `/api/photos` for updates.

## Batch Operations

Not currently supported. Delete/tag photos individually.

## Export/Download

To download multiple photos, make individual requests to the image endpoint.
