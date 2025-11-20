package com.gallery.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageProcessingServiceTest {

    @Autowired
    private ImageProcessingService imageProcessingService;

    @Test
    void testGenerateThumbnail() throws IOException {
        // Arrange
        byte[] imageData = createTestImage(800, 600);

        // Act
        byte[] thumbnail = imageProcessingService.generateThumbnail(imageData);

        // Assert
        assertNotNull(thumbnail);
        assertTrue(thumbnail.length > 0);
        assertTrue(thumbnail.length < imageData.length);
    }

    @Test
    void testGeneratePreview() throws IOException {
        // Arrange
        byte[] imageData = createTestImage(2000, 1500);

        // Act
        byte[] preview = imageProcessingService.generatePreview(imageData);

        // Assert
        assertNotNull(preview);
        assertTrue(preview.length > 0);
        assertTrue(preview.length < imageData.length);
    }

    @Test
    void testGetImageDimensions() throws IOException {
        // Arrange
        byte[] imageData = createTestImage(640, 480);

        // Act
        Map<String, Integer> dimensions = imageProcessingService.getImageDimensions(imageData);

        // Assert
        assertNotNull(dimensions);
        assertEquals(640, dimensions.get("width"));
        assertEquals(480, dimensions.get("height"));
    }

    @Test
    void testIsValidImage() throws IOException {
        // Arrange
        byte[] validImageData = createTestImage(100, 100);
        MockMultipartFile validFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                validImageData
        );

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "not an image".getBytes()
        );

        // Act & Assert
        assertTrue(imageProcessingService.isValidImage(validFile));
        assertFalse(imageProcessingService.isValidImage(invalidFile));
    }

    @Test
    void testGetMimeType() {
        assertEquals("image/jpeg", imageProcessingService.getMimeType("photo.jpg"));
        assertEquals("image/jpeg", imageProcessingService.getMimeType("photo.jpeg"));
        assertEquals("image/png", imageProcessingService.getMimeType("photo.png"));
        assertEquals("image/gif", imageProcessingService.getMimeType("photo.gif"));
        assertEquals("image/webp", imageProcessingService.getMimeType("photo.webp"));
        assertEquals("image/bmp", imageProcessingService.getMimeType("photo.bmp"));
        assertEquals("image/jpeg", imageProcessingService.getMimeType("photo"));
        assertEquals("image/jpeg", imageProcessingService.getMimeType(null));
    }

    @Test
    void testExtractMetadata() throws IOException {
        // Arrange
        byte[] imageData = createTestImage(800, 600);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                imageData
        );

        // Act
        Map<String, Object> metadata = imageProcessingService.extractMetadata(file);

        // Assert
        assertNotNull(metadata);
        // Note: Basic test image won't have EXIF data, so metadata might be empty
        // In a real test, you would use an actual photo with EXIF data
    }

    /**
     * Helper method to create a test image.
     */
    private byte[] createTestImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}
