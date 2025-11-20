package com.gallery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for duplicate check operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckResponse {

    private boolean isDuplicate;
    private UUID existingPhotoId;
    private String message;

    /**
     * Create a response for a duplicate photo.
     *
     * @param existingPhotoId the ID of the existing photo
     * @return the duplicate check response
     */
    public static DuplicateCheckResponse duplicate(UUID existingPhotoId) {
        return DuplicateCheckResponse.builder()
                .isDuplicate(true)
                .existingPhotoId(existingPhotoId)
                .message("Photo already exists")
                .build();
    }

    /**
     * Create a response for a unique photo.
     *
     * @return the duplicate check response
     */
    public static DuplicateCheckResponse unique() {
        return DuplicateCheckResponse.builder()
                .isDuplicate(false)
                .message("Photo is unique")
                .build();
    }
}
