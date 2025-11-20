package com.gallery.service;

import com.gallery.exception.StorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileStorageServiceTest {

    @Autowired
    private FileStorageService fileStorageService;

    private Path testUploadPath;

    @BeforeEach
    void setUp() {
        testUploadPath = Paths.get("./uploads");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files
        if (Files.exists(testUploadPath)) {
            Files.walk(testUploadPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
        }
    }

    @Test
    void testStoreOriginal() {
        // Arrange
        byte[] content = "test image content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                content
        );
        LocalDateTime captureDate = LocalDateTime.of(2024, 1, 15, 10, 30);

        // Act
        String storedPath = fileStorageService.storeOriginal(file, captureDate);

        // Assert
        assertNotNull(storedPath);
        assertTrue(storedPath.contains("2024/01"));
        assertTrue(storedPath.endsWith(".jpg"));
    }

    @Test
    void testStoreOriginalEmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        assertThrows(StorageException.class, () ->
                fileStorageService.storeOriginal(emptyFile, LocalDateTime.now())
        );
    }

    @Test
    void testStoreThumbnail() {
        // Arrange
        byte[] thumbnailData = "thumbnail data".getBytes();
        UUID photoId = UUID.randomUUID();
        String extension = ".jpg";

        // Act
        assertDoesNotThrow(() ->
                fileStorageService.storeThumbnail(thumbnailData, photoId, extension)
        );
    }

    @Test
    void testStorePreview() {
        // Arrange
        byte[] previewData = "preview data".getBytes();
        UUID photoId = UUID.randomUUID();
        String extension = ".jpg";

        // Act
        assertDoesNotThrow(() ->
                fileStorageService.storePreview(previewData, photoId, extension)
        );
    }

    @Test
    void testGetThumbnailPath() {
        // Arrange
        UUID photoId = UUID.randomUUID();
        String extension = ".jpg";

        // Act
        String path = fileStorageService.getThumbnailPath(photoId, extension);

        // Assert
        assertEquals(photoId + extension, path);
    }

    @Test
    void testGetPreviewPath() {
        // Arrange
        UUID photoId = UUID.randomUUID();
        String extension = ".jpg";

        // Act
        String path = fileStorageService.getPreviewPath(photoId, extension);

        // Assert
        assertEquals(photoId + extension, path);
    }
}
