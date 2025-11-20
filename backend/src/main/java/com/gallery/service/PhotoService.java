package com.gallery.service;

import com.gallery.entity.Photo;
import com.gallery.exception.DuplicatePhotoException;
import com.gallery.exception.InvalidFileException;
import com.gallery.exception.PhotoNotFoundException;
import com.gallery.exception.StorageException;
import com.gallery.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for photo management operations.
 * Handles CRUD operations, search, filtering, and duplicate detection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final FileStorageService fileStorageService;
    private final ImageProcessingService imageProcessingService;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    /**
     * Upload and process a new photo.
     * Performs validation, duplicate detection, storage, and metadata extraction.
     *
     * @param file the multipart file to upload
     * @param tags optional tags for the photo
     * @return the saved photo entity
     */
    @Transactional
    public Photo uploadPhoto(MultipartFile file, Set<String> tags) {
        log.info("Uploading photo: {}", file.getOriginalFilename());

        // Validate file
        validateFile(file);

        try {
            // Read file data once
            byte[] fileData = file.getBytes();

            // Calculate file hash for duplicate detection
            String fileHash = calculateSHA256(fileData);

            // Check for duplicates
            Optional<Photo> existingPhoto = photoRepository.findByFileHash(fileHash);
            if (existingPhoto.isPresent()) {
                throw new DuplicatePhotoException(existingPhoto.get().getId());
            }

            // Extract metadata
            Map<String, Object> metadata = imageProcessingService.extractMetadata(file);

            // Get image dimensions if not in metadata
            if (!metadata.containsKey("width") || !metadata.containsKey("height")) {
                Map<String, Integer> dimensions = imageProcessingService.getImageDimensions(fileData);
                metadata.putAll(dimensions);
            }

            // Get capture date or use current time
            LocalDateTime captureDate = (LocalDateTime) metadata.getOrDefault("captureDate", LocalDateTime.now());

            // Store original file
            String storedPath = fileStorageService.storeOriginal(file, captureDate);

            // Create photo entity
            Photo photo = Photo.builder()
                    .originalFilename(file.getOriginalFilename())
                    .storedPath(storedPath)
                    .fileSize(file.getSize())
                    .mimeType(imageProcessingService.getMimeType(file.getOriginalFilename()))
                    .fileHash(fileHash)
                    .captureDate(captureDate)
                    .width((Integer) metadata.get("width"))
                    .height((Integer) metadata.get("height"))
                    .cameraModel((String) metadata.get("cameraModel"))
                    .iso((String) metadata.get("iso"))
                    .aperture((String) metadata.get("aperture"))
                    .shutterSpeed((String) metadata.get("shutterSpeed"))
                    .focalLength((Integer) metadata.get("focalLength"))
                    .tags(tags != null ? tags : new HashSet<>())
                    .build();

            // Save to database first to get the ID
            photo = photoRepository.save(photo);

            try {
                // Generate and store thumbnail
                byte[] thumbnail = imageProcessingService.generateThumbnail(fileData);
                String extension = getFileExtension(file.getOriginalFilename());
                fileStorageService.storeThumbnail(thumbnail, photo.getId(), extension);

                // Generate and store preview
                byte[] preview = imageProcessingService.generatePreview(fileData);
                fileStorageService.storePreview(preview, photo.getId(), extension);

                log.info("Successfully uploaded photo: {} (ID: {})", file.getOriginalFilename(), photo.getId());
                return photo;

            } catch (Exception e) {
                // Rollback: delete the photo and all associated files
                log.error("Failed to process photo, rolling back", e);
                photoRepository.delete(photo);
                fileStorageService.deletePhotoFiles(storedPath, photo.getId());
                throw new StorageException("Failed to process photo", e);
            }

        } catch (IOException e) {
            throw new InvalidFileException("Failed to read file", e);
        }
    }

    /**
     * Get all photos with pagination.
     *
     * @param pageable pagination information
     * @return page of photos
     */
    @Transactional(readOnly = true)
    public Page<Photo> getAllPhotos(Pageable pageable) {
        return photoRepository.findAll(pageable);
    }

    /**
     * Get a photo by ID.
     *
     * @param id the photo ID
     * @return the photo entity
     */
    @Transactional(readOnly = true)
    public Photo getPhotoById(UUID id) {
        return photoRepository.findById(id)
                .orElseThrow(() -> new PhotoNotFoundException(id));
    }

    /**
     * Get photo image data.
     *
     * @param id the photo ID
     * @param type the image type (original, thumbnail, preview)
     * @return the image data as byte array
     */
    @Transactional(readOnly = true)
    public byte[] getPhotoImage(UUID id, String type) {
        Photo photo = getPhotoById(id);

        String relativePath;
        if ("original".equalsIgnoreCase(type)) {
            relativePath = photo.getStoredPath();
        } else {
            String extension = getFileExtension(photo.getOriginalFilename());
            if ("thumbnail".equalsIgnoreCase(type)) {
                relativePath = fileStorageService.getThumbnailPath(id, extension);
            } else if ("preview".equalsIgnoreCase(type)) {
                relativePath = fileStorageService.getPreviewPath(id, extension);
            } else {
                throw new IllegalArgumentException("Invalid image type: " + type);
            }
        }

        return fileStorageService.loadFile(relativePath, type);
    }

    /**
     * Add tags to a photo.
     *
     * @param id the photo ID
     * @param tags the tags to add
     * @return the updated photo
     */
    @Transactional
    public Photo addTags(UUID id, Set<String> tags) {
        Photo photo = getPhotoById(id);
        photo.addTags(tags);
        Photo savedPhoto = photoRepository.save(photo);
        log.info("Added tags to photo {}: {}", id, tags);
        return savedPhoto;
    }

    /**
     * Delete a photo and all associated files.
     *
     * @param id the photo ID
     */
    @Transactional
    public void deletePhoto(UUID id) {
        Photo photo = getPhotoById(id);
        fileStorageService.deletePhotoFiles(photo.getStoredPath(), id);
        photoRepository.delete(photo);
        log.info("Deleted photo: {}", id);
    }

    /**
     * Check if a photo with the given hash already exists.
     *
     * @param fileHash the SHA-256 hash of the file
     * @return the existing photo if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<Photo> checkDuplicate(String fileHash) {
        return photoRepository.findByFileHash(fileHash);
    }

    /**
     * Search photos with multiple criteria.
     *
     * @param tags the tags to search for
     * @param startDate the start date
     * @param endDate the end date
     * @param cameraModel the camera model
     * @param filename the filename
     * @param pageable pagination information
     * @return page of matching photos
     */
    @Transactional(readOnly = true)
    public Page<Photo> searchPhotos(
            Set<String> tags,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String cameraModel,
            String filename,
            Pageable pageable
    ) {
        // Normalize tags to lowercase
        Set<String> normalizedTags = null;
        if (tags != null && !tags.isEmpty()) {
            normalizedTags = new HashSet<>();
            for (String tag : tags) {
                normalizedTags.add(tag.toLowerCase().trim());
            }
        }

        return photoRepository.searchPhotos(
                normalizedTags,
                startDate,
                endDate,
                cameraModel,
                filename,
                pageable
        );
    }

    /**
     * Search photos by tags.
     *
     * @param tags the tags to search for
     * @param pageable pagination information
     * @return page of matching photos
     */
    @Transactional(readOnly = true)
    public Page<Photo> searchByTags(Set<String> tags, Pageable pageable) {
        Set<String> normalizedTags = new HashSet<>();
        for (String tag : tags) {
            normalizedTags.add(tag.toLowerCase().trim());
        }
        return photoRepository.findByTagsIn(normalizedTags, pageable);
    }

    /**
     * Validate uploaded file.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds maximum allowed size of 50MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP, BMP");
        }

        if (!imageProcessingService.isValidImage(file)) {
            throw new InvalidFileException("File is not a valid image or is corrupted");
        }
    }

    /**
     * Calculate SHA-256 hash of file data.
     */
    private String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Extract file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".jpg";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : ".jpg";
    }
}
