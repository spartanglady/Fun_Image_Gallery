package com.gallery.controller;

import com.gallery.dto.*;
import com.gallery.entity.Photo;
import com.gallery.exception.DuplicatePhotoException;
import com.gallery.service.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * REST Controller for photo management operations.
 * Provides endpoints for uploading, retrieving, searching, and managing photos.
 */
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final PhotoService photoService;

    /**
     * Upload a new photo.
     * POST /api/photos/upload
     *
     * @param file the image file
     * @param tags optional comma-separated tags
     * @return upload response with photo details
     */
    @PostMapping("/upload")
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tags", required = false) String tags
    ) {
        try {
            Set<String> tagSet = parseTags(tags);
            Photo photo = photoService.uploadPhoto(file, tagSet);

            PhotoUploadResponse response = PhotoUploadResponse.success(
                    photo.getId(),
                    photo.getOriginalFilename(),
                    photo.getFileSize()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DuplicatePhotoException e) {
            PhotoUploadResponse response = PhotoUploadResponse.builder()
                    .id(e.getExistingPhotoId())
                    .message("Duplicate photo detected")
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    /**
     * Get all photos with pagination.
     * GET /api/photos?page=0&size=50&sort=captureDate,desc
     *
     * @param page page number (default: 0)
     * @param size page size (default: 50)
     * @param sortBy field to sort by (default: captureDate)
     * @param sortDir sort direction (default: desc)
     * @return page of photo DTOs
     */
    @GetMapping
    public ResponseEntity<Page<PhotoDTO>> getAllPhotos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "captureDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Photo> photos = photoService.getAllPhotos(pageable);
        Page<PhotoDTO> photoDTOs = photos.map(PhotoDTO::fromEntity);

        return ResponseEntity.ok(photoDTOs);
    }

    /**
     * Get a specific photo by ID.
     * GET /api/photos/{id}
     *
     * @param id the photo ID
     * @return the photo DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhotoDTO> getPhoto(@PathVariable UUID id) {
        Photo photo = photoService.getPhotoById(id);
        return ResponseEntity.ok(PhotoDTO.fromEntity(photo));
    }

    /**
     * Stream photo image.
     * GET /api/photos/{id}/image?type=thumbnail
     *
     * @param id the photo ID
     * @param type the image type (original, preview, thumbnail)
     * @return the image as byte array with appropriate headers
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getPhotoImage(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "thumbnail") String type
    ) {
        Photo photo = photoService.getPhotoById(id);
        byte[] imageData = photoService.getPhotoImage(id, type);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(photo.getMimeType()));
        headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic());
        headers.setContentLength(imageData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(imageData);
    }

    /**
     * Add tags to a photo.
     * POST /api/photos/{id}/tags
     *
     * @param id the photo ID
     * @param request the tag request
     * @return the updated photo DTO
     */
    @PostMapping("/{id}/tags")
    public ResponseEntity<PhotoDTO> addTags(
            @PathVariable UUID id,
            @RequestBody TagRequest request
    ) {
        Photo photo = photoService.addTags(id, request.getTags());
        return ResponseEntity.ok(PhotoDTO.fromEntity(photo));
    }

    /**
     * Delete a photo.
     * DELETE /api/photos/{id}
     *
     * @param id the photo ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable UUID id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if a file is a duplicate.
     * POST /api/photos/check-duplicate
     *
     * @param request the duplicate check request containing file hash
     * @return duplicate check response
     */
    @PostMapping("/check-duplicate")
    public ResponseEntity<DuplicateCheckResponse> checkDuplicate(
            @RequestBody DuplicateCheckRequest request
    ) {
        Optional<Photo> existingPhoto = photoService.checkDuplicate(request.getFileHash());

        if (existingPhoto.isPresent()) {
            return ResponseEntity.ok(DuplicateCheckResponse.duplicate(existingPhoto.get().getId()));
        } else {
            return ResponseEntity.ok(DuplicateCheckResponse.unique());
        }
    }

    /**
     * Search photos with multiple criteria.
     * GET /api/photos/search?tags=nature,landscape&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59&cameraModel=Canon&query=sunset
     *
     * @param tags comma-separated tags
     * @param startDate start date for capture date range
     * @param endDate end date for capture date range
     * @param cameraModel camera model to filter by
     * @param query filename search query
     * @param page page number
     * @param size page size
     * @param sortBy field to sort by
     * @param sortDir sort direction
     * @return page of matching photo DTOs
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PhotoDTO>> searchPhotos(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String cameraModel,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "captureDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Set<String> tagSet = parseTags(tags);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Photo> photos = photoService.searchPhotos(
                tagSet,
                startDate,
                endDate,
                cameraModel,
                query,
                pageable
        );

        Page<PhotoDTO> photoDTOs = photos.map(PhotoDTO::fromEntity);
        return ResponseEntity.ok(photoDTOs);
    }

    /**
     * Parse comma-separated tags string into a set.
     */
    private Set<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());
    }
}
