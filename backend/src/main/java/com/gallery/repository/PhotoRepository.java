package com.gallery.repository;

import com.gallery.entity.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository interface for Photo entity.
 * Provides database operations and custom queries for photo management.
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    /**
     * Find a photo by its file hash (SHA-256).
     * Used for duplicate detection.
     *
     * @param fileHash the SHA-256 hash of the file
     * @return Optional containing the photo if found
     */
    Optional<Photo> findByFileHash(String fileHash);

    /**
     * Check if a photo with the given file hash exists.
     *
     * @param fileHash the SHA-256 hash of the file
     * @return true if a photo with this hash exists
     */
    boolean existsByFileHash(String fileHash);

    /**
     * Search photos by tags with pagination.
     *
     * @param tags the tags to search for
     * @param pageable pagination information
     * @return page of photos matching the tags
     */
    @Query("SELECT DISTINCT p FROM Photo p JOIN p.tags t WHERE t IN :tags")
    Page<Photo> findByTagsIn(@Param("tags") Set<String> tags, Pageable pageable);

    /**
     * Search photos by camera model.
     *
     * @param cameraModel the camera model to search for
     * @param pageable pagination information
     * @return page of photos matching the camera model
     */
    Page<Photo> findByCameraModelContainingIgnoreCase(String cameraModel, Pageable pageable);

    /**
     * Search photos by original filename.
     *
     * @param filename the filename to search for
     * @param pageable pagination information
     * @return page of photos matching the filename
     */
    Page<Photo> findByOriginalFilenameContainingIgnoreCase(String filename, Pageable pageable);

    /**
     * Search photos by capture date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination information
     * @return page of photos within the date range
     */
    Page<Photo> findByCaptureDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Advanced search with multiple criteria.
     *
     * @param tags the tags to search for
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param cameraModel the camera model to search for
     * @param filename the filename to search for
     * @param pageable pagination information
     * @return page of photos matching all criteria
     */
    @Query("SELECT DISTINCT p FROM Photo p LEFT JOIN p.tags t WHERE " +
           "(:tags IS NULL OR t IN :tags) AND " +
           "(:startDate IS NULL OR p.captureDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.captureDate <= :endDate) AND " +
           "(:cameraModel IS NULL OR LOWER(p.cameraModel) LIKE LOWER(CONCAT('%', :cameraModel, '%'))) AND " +
           "(:filename IS NULL OR LOWER(p.originalFilename) LIKE LOWER(CONCAT('%', :filename, '%')))")
    Page<Photo> searchPhotos(
        @Param("tags") Set<String> tags,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("cameraModel") String cameraModel,
        @Param("filename") String filename,
        Pageable pageable
    );
}
