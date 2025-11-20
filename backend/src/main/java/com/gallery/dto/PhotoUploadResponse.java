package com.gallery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for photo upload operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadResponse {

    private UUID id;
    private String originalFilename;
    private Long fileSize;
    private String message;
    private boolean success;

    /**
     * Create a success response.
     *
     * @param id the photo ID
     * @param originalFilename the original filename
     * @param fileSize the file size
     * @return the upload response
     */
    public static PhotoUploadResponse success(UUID id, String originalFilename, Long fileSize) {
        return PhotoUploadResponse.builder()
                .id(id)
                .originalFilename(originalFilename)
                .fileSize(fileSize)
                .message("Photo uploaded successfully")
                .success(true)
                .build();
    }

    /**
     * Create an error response.
     *
     * @param message the error message
     * @return the upload response
     */
    public static PhotoUploadResponse error(String message) {
        return PhotoUploadResponse.builder()
                .message(message)
                .success(false)
                .build();
    }
}
