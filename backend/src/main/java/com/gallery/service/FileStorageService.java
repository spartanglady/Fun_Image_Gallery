package com.gallery.service;

import com.gallery.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing file storage operations.
 * Handles saving, retrieving, and organizing files on the filesystem.
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${storage.location.original:./uploads/original}")
    private String originalLocation;

    @Value("${storage.location.thumbnail:./uploads/thumbnails}")
    private String thumbnailLocation;

    @Value("${storage.location.preview:./uploads/previews}")
    private String previewLocation;

    private Path originalPath;
    private Path thumbnailPath;
    private Path previewPath;

    /**
     * Initialize storage directories on service startup.
     */
    @PostConstruct
    public void init() {
        try {
            originalPath = Paths.get(originalLocation);
            thumbnailPath = Paths.get(thumbnailLocation);
            previewPath = Paths.get(previewLocation);

            Files.createDirectories(originalPath);
            Files.createDirectories(thumbnailPath);
            Files.createDirectories(previewPath);

            log.info("Storage directories initialized successfully");
            log.info("Original: {}", originalPath.toAbsolutePath());
            log.info("Thumbnail: {}", thumbnailPath.toAbsolutePath());
            log.info("Preview: {}", previewPath.toAbsolutePath());
        } catch (IOException e) {
            throw new StorageException("Failed to initialize storage directories", e);
        }
    }

    /**
     * Store original image file with year/month directory structure.
     *
     * @param file the multipart file to store
     * @param captureDate the capture date (used for directory structure)
     * @return the relative path where the file was stored
     */
    public String storeOriginal(MultipartFile file, LocalDateTime captureDate) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file");
            }

            // Create year/month directory structure
            LocalDateTime date = captureDate != null ? captureDate : LocalDateTime.now();
            String year = String.valueOf(date.getYear());
            String month = String.format("%02d", date.getMonthValue());

            Path yearMonthPath = originalPath.resolve(year).resolve(month);
            Files.createDirectories(yearMonthPath);

            // Generate unique filename
            String extension = getFileExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path destinationFile = yearMonthPath.resolve(filename);

            // Check disk space
            checkDiskSpace(file.getSize());

            // Copy file
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return relative path
            String relativePath = year + "/" + month + "/" + filename;
            log.debug("Stored original file at: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            throw new StorageException("Failed to store original file", e);
        }
    }

    /**
     * Store thumbnail image.
     *
     * @param imageData the image data as byte array
     * @param photoId the UUID of the photo
     * @param extension the file extension
     */
    public void storeThumbnail(byte[] imageData, UUID photoId, String extension) {
        storeProcessedImage(imageData, photoId, extension, thumbnailPath, "thumbnail");
    }

    /**
     * Store preview image.
     *
     * @param imageData the image data as byte array
     * @param photoId the UUID of the photo
     * @param extension the file extension
     */
    public void storePreview(byte[] imageData, UUID photoId, String extension) {
        storeProcessedImage(imageData, photoId, extension, previewPath, "preview");
    }

    /**
     * Store a processed image (thumbnail or preview).
     */
    private void storeProcessedImage(byte[] imageData, UUID photoId, String extension, Path basePath, String type) {
        try {
            String filename = photoId + extension;
            Path destinationFile = basePath.resolve(filename);

            Files.write(destinationFile, imageData);
            log.debug("Stored {} file: {}", type, filename);

        } catch (IOException e) {
            throw new StorageException("Failed to store " + type + " file", e);
        }
    }

    /**
     * Load a file as byte array.
     *
     * @param relativePath the relative path to the file
     * @param type the type of file (original, thumbnail, preview)
     * @return the file content as byte array
     */
    public byte[] loadFile(String relativePath, String type) {
        try {
            Path basePath = getBasePathForType(type);
            Path file = basePath.resolve(relativePath);

            if (!Files.exists(file)) {
                throw new StorageException("File not found: " + relativePath);
            }

            return Files.readAllBytes(file);

        } catch (IOException e) {
            throw new StorageException("Failed to load file: " + relativePath, e);
        }
    }

    /**
     * Delete all files associated with a photo.
     *
     * @param originalRelativePath the relative path to the original file
     * @param photoId the UUID of the photo
     */
    public void deletePhotoFiles(String originalRelativePath, UUID photoId) {
        try {
            // Delete original
            Path originalFile = originalPath.resolve(originalRelativePath);
            Files.deleteIfExists(originalFile);

            // Delete thumbnail
            String extension = getFileExtension(originalRelativePath);
            Path thumbnailFile = thumbnailPath.resolve(photoId + extension);
            Files.deleteIfExists(thumbnailFile);

            // Delete preview
            Path previewFile = previewPath.resolve(photoId + extension);
            Files.deleteIfExists(previewFile);

            log.info("Deleted all files for photo: {}", photoId);

        } catch (IOException e) {
            log.error("Failed to delete files for photo: {}", photoId, e);
            // Don't throw exception, as this is cleanup operation
        }
    }

    /**
     * Get the base path for a given file type.
     */
    private Path getBasePathForType(String type) {
        return switch (type.toLowerCase()) {
            case "original" -> originalPath;
            case "thumbnail" -> thumbnailPath;
            case "preview" -> previewPath;
            default -> throw new IllegalArgumentException("Invalid file type: " + type);
        };
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

    /**
     * Check if there's enough disk space for the upload.
     *
     * @param fileSize the size of the file to be uploaded
     */
    private void checkDiskSpace(long fileSize) {
        try {
            long usableSpace = Files.getFileStore(originalPath).getUsableSpace();
            // Require at least 3x the file size (original + thumbnail + preview + buffer)
            long requiredSpace = fileSize * 3;

            if (usableSpace < requiredSpace) {
                throw new StorageException("Insufficient disk space. Required: " + requiredSpace + " bytes, Available: " + usableSpace + " bytes");
            }
        } catch (IOException e) {
            log.warn("Failed to check disk space", e);
            // Don't fail the upload if we can't check disk space
        }
    }

    /**
     * Get the path for a thumbnail file.
     *
     * @param photoId the UUID of the photo
     * @param extension the file extension
     * @return the relative path to the thumbnail
     */
    public String getThumbnailPath(UUID photoId, String extension) {
        return photoId + extension;
    }

    /**
     * Get the path for a preview file.
     *
     * @param photoId the UUID of the photo
     * @param extension the file extension
     * @return the relative path to the preview
     */
    public String getPreviewPath(UUID photoId, String extension) {
        return photoId + extension;
    }
}
