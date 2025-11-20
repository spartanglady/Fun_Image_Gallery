package com.gallery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String error;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;

    /**
     * Create an error response.
     *
     * @param error the error type
     * @param message the error message
     * @param status the HTTP status code
     * @param path the request path
     * @return the error response
     */
    public static ErrorResponse create(String error, String message, int status, String path) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}
