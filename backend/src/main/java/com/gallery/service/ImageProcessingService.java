package com.gallery.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for image processing operations.
 * Handles thumbnail/preview generation and EXIF metadata extraction.
 */
@Service
@Slf4j
public class ImageProcessingService {

    private static final int THUMBNAIL_SIZE = 300;
    private static final int PREVIEW_SIZE = 1280;
    private static final float QUALITY = 0.85f;

    /**
     * Generate thumbnail image (300px max dimension).
     *
     * @param imageData the original image data
     * @return the thumbnail image as byte array
     */
    public byte[] generateThumbnail(byte[] imageData) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(new ByteArrayInputStream(imageData))
                    .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .outputQuality(QUALITY)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            log.debug("Generated thumbnail: {} bytes", outputStream.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new com.gallery.exception.ImageProcessingException("Failed to generate thumbnail", e);
        }
    }

    /**
     * Generate preview image (1280px max dimension).
     *
     * @param imageData the original image data
     * @return the preview image as byte array
     */
    public byte[] generatePreview(byte[] imageData) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(new ByteArrayInputStream(imageData))
                    .size(PREVIEW_SIZE, PREVIEW_SIZE)
                    .outputQuality(QUALITY)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            log.debug("Generated preview: {} bytes", outputStream.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new com.gallery.exception.ImageProcessingException("Failed to generate preview", e);
        }
    }

    /**
     * Extract EXIF metadata from an image file.
     *
     * @param file the multipart file
     * @return map containing extracted metadata
     */
    public Map<String, Object> extractMetadata(MultipartFile file) {
        Map<String, Object> metadataMap = new HashMap<>();

        try (InputStream inputStream = file.getInputStream()) {
            // Read metadata using metadata-extractor
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);

            // Extract camera model
            ExifIFD0Directory exifIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIFD0 != null) {
                String make = exifIFD0.getString(ExifIFD0Directory.TAG_MAKE);
                String model = exifIFD0.getString(ExifIFD0Directory.TAG_MODEL);
                if (make != null && model != null) {
                    metadataMap.put("cameraModel", make.trim() + " " + model.trim());
                } else if (model != null) {
                    metadataMap.put("cameraModel", model.trim());
                }
            }

            // Extract EXIF SubIFD data (ISO, aperture, shutter speed, etc.)
            ExifSubIFDDirectory exifSubIFD = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifSubIFD != null) {
                // ISO
                Integer iso = exifSubIFD.getInteger(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
                if (iso != null) {
                    metadataMap.put("iso", iso.toString());
                }

                // Aperture
                String aperture = exifSubIFD.getString(ExifSubIFDDirectory.TAG_FNUMBER);
                if (aperture != null) {
                    metadataMap.put("aperture", "f/" + aperture);
                }

                // Shutter Speed
                String shutterSpeed = exifSubIFD.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
                if (shutterSpeed != null) {
                    metadataMap.put("shutterSpeed", shutterSpeed + "s");
                }

                // Focal Length
                String focalLengthStr = exifSubIFD.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
                if (focalLengthStr != null) {
                    try {
                        // Parse focal length (e.g., "50 mm" or "50")
                        String[] parts = focalLengthStr.split(" ");
                        int focalLength = (int) Double.parseDouble(parts[0]);
                        metadataMap.put("focalLength", focalLength);
                    } catch (NumberFormatException e) {
                        log.debug("Failed to parse focal length: {}", focalLengthStr);
                    }
                }

                // Capture Date
                Date captureDate = exifSubIFD.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (captureDate != null) {
                    LocalDateTime localDateTime = captureDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    metadataMap.put("captureDate", localDateTime);
                }
            }

            // Extract image dimensions
            extractDimensions(metadata, metadataMap);

            log.debug("Extracted metadata: {}", metadataMap);

        } catch (ImageProcessingException | IOException e) {
            log.warn("Failed to extract metadata from file: {}", file.getOriginalFilename(), e);
            // Don't throw exception, metadata extraction is optional
        }

        return metadataMap;
    }

    /**
     * Extract image dimensions from metadata.
     */
    private void extractDimensions(Metadata metadata, Map<String, Object> metadataMap) {
        // Try JPEG directory first
        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        if (jpegDirectory != null) {
            Integer width = jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);
            Integer height = jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);
            if (width != null && height != null) {
                metadataMap.put("width", width);
                metadataMap.put("height", height);
                return;
            }
        }

        // Try EXIF directory
        ExifSubIFDDirectory exifSubIFD = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exifSubIFD != null) {
            Integer width = exifSubIFD.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
            Integer height = exifSubIFD.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
            if (width != null && height != null) {
                metadataMap.put("width", width);
                metadataMap.put("height", height);
                return;
            }
        }

        // Fallback: read image dimensions directly
        try (InputStream inputStream = new ByteArrayInputStream(metadataMap.toString().getBytes())) {
            // This is a placeholder - we need the actual image data
            log.debug("Could not extract dimensions from metadata");
        } catch (Exception e) {
            log.debug("Failed to extract dimensions", e);
        }
    }

    /**
     * Get image dimensions from byte array.
     *
     * @param imageData the image data
     * @return map containing width and height
     */
    public Map<String, Integer> getImageDimensions(byte[] imageData) {
        Map<String, Integer> dimensions = new HashMap<>();
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image != null) {
                dimensions.put("width", image.getWidth());
                dimensions.put("height", image.getHeight());
            }
        } catch (IOException e) {
            log.warn("Failed to read image dimensions", e);
        }
        return dimensions;
    }

    /**
     * Validate that the file is a valid image.
     *
     * @param file the multipart file to validate
     * @return true if the file is a valid image
     */
    public boolean isValidImage(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            return image != null;
        } catch (IOException e) {
            log.warn("Invalid image file: {}", file.getOriginalFilename());
            return false;
        }
    }

    /**
     * Get the appropriate MIME type for an image file.
     *
     * @param filename the filename
     * @return the MIME type
     */
    public String getMimeType(String filename) {
        if (filename == null) {
            return "image/jpeg";
        }

        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".bmp")) {
            return "image/bmp";
        } else {
            return "image/jpeg";
        }
    }
}
