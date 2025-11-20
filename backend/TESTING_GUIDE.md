# Photo Gallery Backend Testing Guide

This guide provides step-by-step instructions to test all API endpoints.

## Prerequisites

1. Start the backend server:
```bash
./start.sh
# or
./gradlew bootRun
```

2. Verify the server is running:
```bash
curl http://localhost:8080/actuator/health || echo "Server is running on port 8080"
```

## Test Scenarios

### Scenario 1: Upload and Retrieve Photos

#### Step 1: Upload a Photo
```bash
# Replace /path/to/photo.jpg with an actual image file
curl -X POST http://localhost:8080/api/photos/upload \
  -F "file=@/path/to/photo.jpg" \
  -F "tags=nature,landscape" \
  -v

# Expected: 201 Created with photo ID
```

**Sample Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "photo.jpg",
  "fileSize": 2048576,
  "message": "Photo uploaded successfully",
  "success": true
}
```

**Save the `id` value for subsequent tests!**

#### Step 2: Get All Photos
```bash
curl http://localhost:8080/api/photos?page=0&size=10 | jq '.'

# Expected: 200 OK with paginated photo list
```

#### Step 3: Get Photo Details
```bash
# Replace {id} with the actual photo ID from Step 1
curl http://localhost:8080/api/photos/{id} | jq '.'

# Expected: 200 OK with complete photo metadata
```

#### Step 4: Download Thumbnail
```bash
curl http://localhost:8080/api/photos/{id}/image?type=thumbnail \
  -o thumbnail.jpg

# Check the file
ls -lh thumbnail.jpg
open thumbnail.jpg  # macOS
```

#### Step 5: Download Preview
```bash
curl http://localhost:8080/api/photos/{id}/image?type=preview \
  -o preview.jpg

# Check the file
ls -lh preview.jpg
```

#### Step 6: Download Original
```bash
curl http://localhost:8080/api/photos/{id}/image?type=original \
  -o original.jpg

# Check the file
ls -lh original.jpg
```

---

### Scenario 2: Tag Management

#### Step 1: Add Tags to Photo
```bash
curl -X POST http://localhost:8080/api/photos/{id}/tags \
  -H "Content-Type: application/json" \
  -d '{"tags": ["sunset", "beach", "vacation"]}' \
  | jq '.'

# Expected: 200 OK with updated photo including new tags
```

#### Step 2: Verify Tags Were Added
```bash
curl http://localhost:8080/api/photos/{id} | jq '.tags'

# Expected: Array containing all tags including newly added ones
```

---

### Scenario 3: Duplicate Detection

#### Step 1: Calculate File Hash (requires Node.js/Python)

**Using Python:**
```bash
python3 << 'EOF'
import hashlib
with open('/path/to/photo.jpg', 'rb') as f:
    file_hash = hashlib.sha256(f.read()).hexdigest()
    print(file_hash)
EOF
```

**Using Node.js:**
```bash
node << 'EOF'
const crypto = require('crypto');
const fs = require('fs');
const hash = crypto.createHash('sha256');
hash.update(fs.readFileSync('/path/to/photo.jpg'));
console.log(hash.digest('hex'));
EOF
```

#### Step 2: Check for Duplicate
```bash
# Replace {hash} with the actual hash from Step 1
curl -X POST http://localhost:8080/api/photos/check-duplicate \
  -H "Content-Type: application/json" \
  -d '{"fileHash": "{hash}"}' \
  | jq '.'

# Expected: isDuplicate: true if photo was already uploaded
```

#### Step 3: Try Uploading the Same File Again
```bash
curl -X POST http://localhost:8080/api/photos/upload \
  -F "file=@/path/to/photo.jpg" \
  -v

# Expected: 409 Conflict - Duplicate photo detected
```

---

### Scenario 4: Search and Filtering

#### Step 1: Upload Multiple Photos with Different Tags
```bash
# Photo 1
curl -X POST http://localhost:8080/api/photos/upload \
  -F "file=@photo1.jpg" \
  -F "tags=nature,landscape"

# Photo 2
curl -X POST http://localhost:8080/api/photos/upload \
  -F "file=@photo2.jpg" \
  -F "tags=portrait,people"

# Photo 3
curl -X POST http://localhost:8080/api/photos/upload \
  -F "file=@photo3.jpg" \
  -F "tags=nature,wildlife"
```

#### Step 2: Search by Tags
```bash
# Find all photos with "nature" tag
curl "http://localhost:8080/api/photos/search?tags=nature" | jq '.content[].tags'

# Find photos with multiple tags
curl "http://localhost:8080/api/photos/search?tags=nature,landscape" | jq '.content[].originalFilename'
```

#### Step 3: Search by Date Range
```bash
# Photos from 2024
curl "http://localhost:8080/api/photos/search?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" \
  | jq '.content[].captureDate'
```

#### Step 4: Search by Camera Model
```bash
# Photos taken with Canon cameras
curl "http://localhost:8080/api/photos/search?cameraModel=Canon" \
  | jq '.content[].cameraModel'
```

#### Step 5: Search by Filename
```bash
# Photos with "sunset" in filename
curl "http://localhost:8080/api/photos/search?query=sunset" \
  | jq '.content[].originalFilename'
```

#### Step 6: Combined Search
```bash
# Nature photos taken with Canon in 2024
curl "http://localhost:8080/api/photos/search?tags=nature&cameraModel=Canon&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" \
  | jq '.totalElements'
```

---

### Scenario 5: Pagination and Sorting

#### Step 1: Get First Page
```bash
curl "http://localhost:8080/api/photos?page=0&size=5" \
  | jq '{totalElements, totalPages, first, last}'
```

#### Step 2: Get Second Page
```bash
curl "http://localhost:8080/api/photos?page=1&size=5" \
  | jq '.content[].originalFilename'
```

#### Step 3: Sort by Filename Ascending
```bash
curl "http://localhost:8080/api/photos?sortBy=originalFilename&sortDir=asc" \
  | jq '.content[].originalFilename'
```

#### Step 4: Sort by File Size Descending
```bash
curl "http://localhost:8080/api/photos?sortBy=fileSize&sortDir=desc" \
  | jq '.content[] | {filename: .originalFilename, size: .fileSize}'
```

---

### Scenario 6: Error Handling

#### Test 1: Upload Empty File
```bash
touch empty.jpg
curl -X POST http://localhost:8080/api/photos/upload \
  -F "file=@empty.jpg" \
  -v

# Expected: 400 Bad Request
```

#### Test 2: Upload Non-Image File
```bash
echo "not an image" > test.txt
curl -X POST http://localhost:8080/api/photos/upload \
  -F "file=@test.txt" \
  -v

# Expected: 400 Bad Request - Invalid file type
```

#### Test 3: Get Non-Existent Photo
```bash
curl http://localhost:8080/api/photos/00000000-0000-0000-0000-000000000000 \
  -v

# Expected: 404 Not Found
```

#### Test 4: Invalid Image Type Parameter
```bash
curl "http://localhost:8080/api/photos/{id}/image?type=invalid" \
  -v

# Expected: 400 Bad Request
```

---

### Scenario 7: Delete Photos

#### Step 1: Delete a Photo
```bash
curl -X DELETE http://localhost:8080/api/photos/{id} \
  -v

# Expected: 204 No Content
```

#### Step 2: Verify Photo Was Deleted
```bash
curl http://localhost:8080/api/photos/{id} \
  -v

# Expected: 404 Not Found
```

#### Step 3: Verify Files Were Deleted
```bash
# Check that uploaded files were removed
ls -la ./uploads/original/
ls -la ./uploads/thumbnails/
ls -la ./uploads/previews/
```

---

## Performance Testing

### Test Upload Performance
```bash
# Time multiple uploads
time for i in {1..10}; do
  curl -X POST http://localhost:8080/api/photos/upload \
    -F "file=@photo.jpg" \
    -F "tags=test" \
    -o /dev/null -s
done
```

### Test Thumbnail Load Time
```bash
# Time thumbnail download
time curl http://localhost:8080/api/photos/{id}/image?type=thumbnail \
  -o thumbnail.jpg -s
```

### Test Pagination Performance
```bash
# Time large page retrieval
time curl "http://localhost:8080/api/photos?page=0&size=100" \
  -o /dev/null -s
```

---

## Load Testing with Apache Bench (Optional)

### Install Apache Bench
```bash
# macOS (already installed)
which ab

# Ubuntu/Debian
sudo apt-get install apache2-utils
```

### Test Image Retrieval
```bash
# 100 requests, 10 concurrent
ab -n 100 -c 10 http://localhost:8080/api/photos/{id}/image?type=thumbnail
```

### Test Photo Listing
```bash
ab -n 100 -c 10 "http://localhost:8080/api/photos?page=0&size=50"
```

---

## Database Inspection

### Access H2 Console
1. Open browser: http://localhost:8080/h2-console
2. Connection settings:
   - JDBC URL: `jdbc:h2:file:./data/gallery`
   - Username: `sa`
   - Password: (leave blank)
3. Click "Connect"

### Useful SQL Queries

```sql
-- Count total photos
SELECT COUNT(*) FROM photos;

-- View all photos with tags
SELECT p.id, p.original_filename, GROUP_CONCAT(pt.tag) as tags
FROM photos p
LEFT JOIN photo_tags pt ON p.id = pt.photo_id
GROUP BY p.id;

-- Photos by camera model
SELECT camera_model, COUNT(*) as count
FROM photos
GROUP BY camera_model;

-- Largest photos
SELECT original_filename, file_size
FROM photos
ORDER BY file_size DESC
LIMIT 10;

-- Photos by capture date
SELECT DATE(capture_date) as date, COUNT(*) as count
FROM photos
GROUP BY DATE(capture_date)
ORDER BY date DESC;
```

---

## Troubleshooting

### Server Won't Start
```bash
# Check Java version
java -version

# Check if port 8080 is already in use
lsof -i :8080

# View logs
./gradlew bootRun --info
```

### Uploads Failing
```bash
# Check disk space
df -h

# Check upload directory permissions
ls -la ./uploads/

# Create directories manually if needed
mkdir -p ./uploads/{original,thumbnails,previews}
```

### Database Issues
```bash
# Delete database and restart
rm -rf ./data/
./gradlew bootRun
```

### Out of Memory Errors
```bash
# Increase JVM heap size
export JAVA_OPTS="-Xmx2g -Xms512m"
./gradlew bootRun
```

---

## Cleanup After Testing

```bash
# Stop the server (Ctrl+C)

# Delete test uploads
rm -rf ./uploads/

# Delete test database
rm -rf ./data/

# Remove test files
rm -f thumbnail.jpg preview.jpg original.jpg empty.jpg test.txt
```

---

## Automated Testing Script

Create a file `test-all.sh`:

```bash
#!/bin/bash

echo "Photo Gallery API Test Suite"
echo "============================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

# Test function
test_endpoint() {
    local name=$1
    local command=$2
    local expected_code=$3

    echo -n "Testing: $name... "

    response=$(eval "$command" -w "%{http_code}" -o /dev/null -s)

    if [ "$response" == "$expected_code" ]; then
        echo -e "${GREEN}PASSED${NC}"
        ((PASSED++))
    else
        echo -e "${RED}FAILED${NC} (Expected: $expected_code, Got: $response)"
        ((FAILED++))
    fi
}

# Run tests
test_endpoint "Get all photos" \
    "curl http://localhost:8080/api/photos" \
    "200"

test_endpoint "Get non-existent photo" \
    "curl http://localhost:8080/api/photos/00000000-0000-0000-0000-000000000000" \
    "404"

test_endpoint "Search photos" \
    "curl 'http://localhost:8080/api/photos/search?tags=nature'" \
    "200"

echo ""
echo "============================"
echo "Results: ${GREEN}$PASSED passed${NC}, ${RED}$FAILED failed${NC}"
echo "============================"
```

Make it executable and run:
```bash
chmod +x test-all.sh
./test-all.sh
```

---

## Next Steps

1. **Frontend Integration:** Use these endpoints in your React frontend
2. **Real Photos:** Test with actual photos containing EXIF data
3. **Performance:** Monitor response times with realistic data volumes
4. **Edge Cases:** Test with unusual filenames, large files, etc.
