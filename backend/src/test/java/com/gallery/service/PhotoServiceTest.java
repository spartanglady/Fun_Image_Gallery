package com.gallery.service;

import com.gallery.entity.Photo;
import com.gallery.exception.DuplicatePhotoException;
import com.gallery.exception.InvalidFileException;
import com.gallery.exception.PhotoNotFoundException;
import com.gallery.repository.PhotoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PhotoServiceTest {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private PhotoRepository photoRepository;

    @BeforeEach
    void setUp() {
        photoRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        photoRepository.deleteAll();
    }

    @Test
    void testUploadPhoto() throws IOException {
        // Arrange
        byte[] imageData = createTestImage(800, 600);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-photo.jpg",
                "image/jpeg",
                imageData
        );
        Set<String> tags = Set.of("nature", "landscape");

        // Act
        Photo photo = photoService.uploadPhoto(file, tags);

        // Assert
        assertNotNull(photo);
        assertNotNull(photo.getId());
        assertEquals("test-photo.jpg", photo.getOriginalFilename());
        assertEquals(imageData.length, photo.getFileSize());
        assertEquals("image/jpeg", photo.getMimeType());
        assertTrue(photo.getTags().contains("nature"));
        assertTrue(photo.getTags().contains("landscape"));
    }

    @Test
    void testUploadPhotoWithEmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        assertThrows(InvalidFileException.class, () ->
                photoService.uploadPhoto(emptyFile, null)
        );
    }

    @Test
    void testUploadPhotoWithInvalidType() {
        // Arrange
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "not an image".getBytes()
        );

        // Act & Assert
        assertThrows(InvalidFileException.class, () ->
                photoService.uploadPhoto(textFile, null)
        );
    }

    @Test
    void testUploadDuplicatePhoto() throws IOException {
        // Arrange
        byte[] imageData = createTestImage(800, 600);
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "photo1.jpg",
                "image/jpeg",
                imageData
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "photo2.jpg",
                "image/jpeg",
                imageData
        );

        // Act
        Photo photo1 = photoService.uploadPhoto(file1, null);

        // Assert
        assertThrows(DuplicatePhotoException.class, () ->
                photoService.uploadPhoto(file2, null)
        );
    }

    @Test
    void testGetAllPhotos() throws IOException {
        // Arrange
        uploadTestPhoto("photo1.jpg");
        uploadTestPhoto("photo2.jpg");
        uploadTestPhoto("photo3.jpg");

        // Act
        Page<Photo> photos = photoService.getAllPhotos(PageRequest.of(0, 10));

        // Assert
        assertNotNull(photos);
        assertEquals(3, photos.getTotalElements());
    }

    @Test
    void testGetPhotoById() throws IOException {
        // Arrange
        Photo uploadedPhoto = uploadTestPhoto("test.jpg");

        // Act
        Photo retrievedPhoto = photoService.getPhotoById(uploadedPhoto.getId());

        // Assert
        assertNotNull(retrievedPhoto);
        assertEquals(uploadedPhoto.getId(), retrievedPhoto.getId());
        assertEquals(uploadedPhoto.getOriginalFilename(), retrievedPhoto.getOriginalFilename());
    }

    @Test
    void testGetPhotoByIdNotFound() {
        // Act & Assert
        assertThrows(PhotoNotFoundException.class, () ->
                photoService.getPhotoById(UUID.randomUUID())
        );
    }

    @Test
    void testAddTags() throws IOException {
        // Arrange
        Photo photo = uploadTestPhoto("test.jpg");
        Set<String> newTags = Set.of("sunset", "beach");

        // Act
        Photo updatedPhoto = photoService.addTags(photo.getId(), newTags);

        // Assert
        assertNotNull(updatedPhoto);
        assertTrue(updatedPhoto.getTags().contains("sunset"));
        assertTrue(updatedPhoto.getTags().contains("beach"));
    }

    @Test
    void testDeletePhoto() throws IOException {
        // Arrange
        Photo photo = uploadTestPhoto("test.jpg");
        UUID photoId = photo.getId();

        // Act
        photoService.deletePhoto(photoId);

        // Assert
        assertThrows(PhotoNotFoundException.class, () ->
                photoService.getPhotoById(photoId)
        );
    }

    @Test
    void testCheckDuplicateExists() throws IOException {
        // Arrange
        Photo photo = uploadTestPhoto("test.jpg");

        // Act
        Optional<Photo> duplicate = photoService.checkDuplicate(photo.getFileHash());

        // Assert
        assertTrue(duplicate.isPresent());
        assertEquals(photo.getId(), duplicate.get().getId());
    }

    @Test
    void testCheckDuplicateNotExists() {
        // Act
        Optional<Photo> duplicate = photoService.checkDuplicate("nonexistent-hash");

        // Assert
        assertFalse(duplicate.isPresent());
    }

    /**
     * Helper method to upload a test photo.
     */
    private Photo uploadTestPhoto(String filename) throws IOException {
        byte[] imageData = createTestImage(800, 600);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                imageData
        );
        return photoService.uploadPhoto(file, new HashSet<>());
    }

    /**
     * Helper method to create a test image.
     */
    private byte[] createTestImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}
