package com.gallery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for adding tags to a photo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagRequest {

    private Set<String> tags;
}
