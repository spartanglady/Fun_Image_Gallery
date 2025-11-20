package com.gallery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity representing a photo in the gallery.
 * Stores metadata to avoid file system reads on every request.
 */
@Entity
@Table(name = "photos", indexes = {
    @Index(name = "idx_capture_date", columnList = "captureDate"),
    @Index(name = "idx_camera_model", columnList = "cameraModel"),
    @Index(name = "idx_file_hash", columnList = "fileHash", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false, unique = true)
    private String storedPath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String mimeType;

    private LocalDateTime captureDate;

    private Integer width;

    private Integer height;

    private String cameraModel;

    private String iso;

    private String aperture;

    private String shutterSpeed;

    private Integer focalLength;

    @Column(unique = true, nullable = false)
    private String fileHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "photo_tags", joinColumns = @JoinColumn(name = "photo_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Adds a tag to the photo.
     * @param tag the tag to add
     */
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        this.tags.add(tag.toLowerCase().trim());
    }

    /**
     * Adds multiple tags to the photo.
     * @param tags the tags to add
     */
    public void addTags(Set<String> tags) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        tags.forEach(tag -> this.tags.add(tag.toLowerCase().trim()));
    }
}
