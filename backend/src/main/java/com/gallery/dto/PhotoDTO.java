package com.gallery.dto;

import com.gallery.entity.Photo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for Photo entity.
 * Used for API responses to avoid exposing the entity directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDTO {

    private UUID id;
    private String originalFilename;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime captureDate;
    private Integer width;
    private Integer height;
    private String cameraModel;
    private String iso;
    private String aperture;
    private String shutterSpeed;
    private Integer focalLength;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Photo entity to DTO.
     *
     * @param photo the photo entity
     * @return the photo DTO
     */
    public static PhotoDTO fromEntity(Photo photo) {
        return PhotoDTO.builder()
                .id(photo.getId())
                .originalFilename(photo.getOriginalFilename())
                .fileSize(photo.getFileSize())
                .mimeType(photo.getMimeType())
                .captureDate(photo.getCaptureDate())
                .width(photo.getWidth())
                .height(photo.getHeight())
                .cameraModel(photo.getCameraModel())
                .iso(photo.getIso())
                .aperture(photo.getAperture())
                .shutterSpeed(photo.getShutterSpeed())
                .focalLength(photo.getFocalLength())
                .tags(photo.getTags())
                .createdAt(photo.getCreatedAt())
                .updatedAt(photo.getUpdatedAt())
                .build();
    }
}
