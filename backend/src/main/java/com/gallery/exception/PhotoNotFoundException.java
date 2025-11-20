package com.gallery.exception;

import java.util.UUID;

/**
 * Exception thrown when a photo is not found.
 */
public class PhotoNotFoundException extends RuntimeException {

    public PhotoNotFoundException(UUID id) {
        super("Photo not found with id: " + id);
    }

    public PhotoNotFoundException(String message) {
        super(message);
    }
}
