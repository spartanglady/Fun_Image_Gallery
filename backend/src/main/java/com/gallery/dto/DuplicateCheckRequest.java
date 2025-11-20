package com.gallery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for checking duplicate photos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckRequest {

    private String fileHash;
}
