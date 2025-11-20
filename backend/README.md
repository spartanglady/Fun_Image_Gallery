# Photo Gallery Backend

Spring Boot backend for the Photo Gallery application.

## Tech Stack

- **Framework:** Spring Boot 3.2.0
- **Language:** Java 17+
- **Build Tool:** Gradle
- **Database:** H2 (file-based)
- **ORM:** Spring Data JPA
- **Image Processing:** Thumbnailator 0.4.19
- **Metadata Extraction:** metadata-extractor 2.18.0

## Features

- Photo upload with automatic thumbnail (300px) and preview (1280px) generation
- EXIF metadata extraction (camera model, ISO, aperture, shutter speed, focal length, capture date)
- SHA-256 hash-based duplicate detection
- Paginated photo listing with sorting
- Advanced search by tags, date range, camera model, and filename
- Tag management
- Efficient image streaming with browser caching (Cache-Control headers)
- Comprehensive error handling with rollback on failures
- File validation (type, size, corruption)
- Disk space checking before uploads

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/gallery/
│   │   │   ├── PhotoGalleryApplication.java
│   │   │   ├── config/          # CORS and storage configuration
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── exception/       # Custom exceptions and global handler
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   └── service/         # Business logic services
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/gallery/service/  # Unit tests
├── build.gradle
└── README.md
```

## API Endpoints

### Photo Upload
- **POST** `/api/photos/upload`
  - Accepts multipart file upload
  - Optional tags (comma-separated)
  - Returns photo ID and metadata
  - Automatically generates thumbnails and previews

### Photo Listing
- **GET** `/api/photos?page=0&size=50&sort=captureDate,desc`
  - Returns paginated list of photos
  - Supports sorting by any field
  - Default: 50 photos per page, sorted by capture date (newest first)

### Photo Details
- **GET** `/api/photos/{id}`
  - Returns complete photo metadata

### Image Retrieval
- **GET** `/api/photos/{id}/image?type=thumbnail`
  - Query param `type`: `original`, `preview`, or `thumbnail`
  - Returns image with Cache-Control headers (1 day)
  - Supports efficient byte streaming

### Tag Management
- **POST** `/api/photos/{id}/tags`
  - Body: `{ "tags": ["nature", "landscape"] }`
  - Adds tags to a photo

### Delete Photo
- **DELETE** `/api/photos/{id}`
  - Deletes photo and all associated files

### Duplicate Check
- **POST** `/api/photos/check-duplicate`
  - Body: `{ "fileHash": "sha256-hash" }`
  - Returns whether photo already exists

### Advanced Search
- **GET** `/api/photos/search?tags=nature,landscape&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59&cameraModel=Canon&query=sunset`
  - All parameters optional
  - Supports pagination and sorting
  - Multiple criteria combined with AND logic

## Running the Application

### Prerequisites
- Java 17 or higher
- No additional setup required (H2 database and Gradle wrapper included)

### Start the Server

```bash
# On Unix/macOS
./gradlew bootRun

# On Windows
gradlew.bat bootRun
```

The server will start on **http://localhost:8080**

### Build the Project

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

## Configuration

Edit `src/main/resources/application.properties` to customize:

- **Server Port:** `server.port=8080`
- **Database Location:** `spring.datasource.url=jdbc:h2:file:./data/gallery`
- **Max Upload Size:** `spring.servlet.multipart.max-file-size=50MB`
- **Storage Paths:**
  - `storage.location.original=./uploads/original`
  - `storage.location.thumbnail=./uploads/thumbnails`
  - `storage.location.preview=./uploads/previews`
- **CORS Origins:** `cors.allowed-origins=http://localhost:5173`

## Storage Structure

Files are organized to prevent filesystem bottlenecks:

```
uploads/
├── original/
│   └── {year}/
│       └── {month}/
│           └── {uuid}.jpg
├── thumbnails/
│   └── {uuid}.jpg
└── previews/
    └── {uuid}.jpg
```

## H2 Console (Development)

Access the H2 database console at: **http://localhost:8080/h2-console**

- **JDBC URL:** `jdbc:h2:file:./data/gallery`
- **Username:** `sa`
- **Password:** (leave blank)

## Sample curl Commands

### Upload a Photo
```bash
curl -X POST http://localhost:8080/api/photos/upload \
  -F "file=@/path/to/photo.jpg" \
  -F "tags=nature,landscape"
```

### Get All Photos
```bash
curl http://localhost:8080/api/photos?page=0&size=10
```

### Get Photo Thumbnail
```bash
curl http://localhost:8080/api/photos/{photo-id}/image?type=thumbnail \
  -o thumbnail.jpg
```

### Search Photos
```bash
curl "http://localhost:8080/api/photos/search?tags=nature&cameraModel=Canon"
```

### Add Tags
```bash
curl -X POST http://localhost:8080/api/photos/{photo-id}/tags \
  -H "Content-Type: application/json" \
  -d '{"tags": ["sunset", "beach"]}'
```

### Check Duplicate
```bash
curl -X POST http://localhost:8080/api/photos/check-duplicate \
  -H "Content-Type: application/json" \
  -d '{"fileHash": "abc123..."}'
```

### Delete Photo
```bash
curl -X DELETE http://localhost:8080/api/photos/{photo-id}
```

## Error Handling

The application provides consistent error responses:

```json
{
  "error": "Photo Not Found",
  "message": "Photo not found with id: 12345",
  "status": 404,
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/photos/12345"
}
```

Error types:
- **400 Bad Request:** Invalid file, invalid arguments
- **404 Not Found:** Photo not found
- **409 Conflict:** Duplicate photo
- **413 Payload Too Large:** File size exceeds 50MB
- **500 Internal Server Error:** Storage or processing errors

## Testing

The project includes unit tests for:
- FileStorageService
- ImageProcessingService
- PhotoService (CRUD operations)

Run tests with: `./gradlew test`

## Performance Optimizations

- Browser caching with Cache-Control headers (1 day max-age)
- Efficient byte streaming for image delivery
- Pagination for all list endpoints
- Database indexes on capture date, camera model, and file hash
- Year/month directory structure prevents single-folder bottlenecks

## Limitations

- H2 database suitable for <10k photos (consider PostgreSQL for larger galleries)
- Local filesystem storage only (no cloud storage integration)
- Photos only (no video support)
- Single-user (no authentication/authorization)

## Next Steps

1. **Frontend Integration:** Connect React frontend to these endpoints
2. **Database Migration:** Consider PostgreSQL for larger galleries
3. **Cloud Storage:** Add S3 or similar for scalability
4. **Authentication:** Add user management and security
5. **Batch Operations:** Implement bulk delete/tag operations
