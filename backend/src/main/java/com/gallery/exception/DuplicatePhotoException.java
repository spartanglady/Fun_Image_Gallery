package com.gallery.exception;

import java.util.UUID;

/**
 * Exception thrown when attempting to upload a duplicate photo.
 */
public class DuplicatePhotoException extends RuntimeException {

    private final UUID existingPhotoId;

    public DuplicatePhotoException(UUID existingPhotoId) {
        super("Duplicate photo detected. Photo already exists with id: " + existingPhotoId);
        this.existingPhotoId = existingPhotoId;
    }

    public UUID getExistingPhotoId() {
        return existingPhotoId;
    }
}
