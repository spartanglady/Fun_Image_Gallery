package com.gallery.exception;

import com.gallery.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Global exception handler for the application.
 * Provides consistent error responses across all endpoints.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle PhotoNotFoundException.
     */
    @ExceptionHandler(PhotoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePhotoNotFoundException(
            PhotoNotFoundException ex,
            WebRequest request
    ) {
        log.error("Photo not found: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.create(
                "Photo Not Found",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle DuplicatePhotoException.
     */
    @ExceptionHandler(DuplicatePhotoException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePhotoException(
            DuplicatePhotoException ex,
            WebRequest request
    ) {
        log.warn("Duplicate photo detected: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.create(
                "Duplicate Photo",
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle InvalidFileException.
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileException(
            InvalidFileException ex,
            WebRequest request
    ) {
        log.error("Invalid file: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.create(
                "Invalid File",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle StorageException.
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(
            StorageException ex,
            WebRequest request
    ) {
        log.error("Storage error: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.create(
                "Storage Error",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle ImageProcessingException.
     */
    @ExceptionHandler(ImageProcessingException.class)
    public ResponseEntity<ErrorResponse> handleImageProcessingException(
            ImageProcessingException ex,
            WebRequest request
    ) {
        log.error("Image processing error: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.create(
                "Image Processing Error",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle MaxUploadSizeExceededException.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            WebRequest request
    ) {
        log.error("File size exceeded: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.create(
                "File Too Large",
                "File size exceeds maximum allowed size of 50MB",
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    /**
     * Handle IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request
    ) {
        log.error("Invalid argument: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.create(
                "Invalid Argument",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.create(
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
