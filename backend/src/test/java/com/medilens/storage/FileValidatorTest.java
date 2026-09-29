package com.medilens.storage;

import com.medilens.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileValidatorTest {

    private FileValidator fileValidator;

    @BeforeEach
    void setUp() {
        fileValidator = new FileValidator();
    }

    @Test
    @DisplayName("Should accept valid PDF file with magic bytes")
    void testValidPdf() {
        byte[] pdfContent = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, '1', '.', '4', '\n'};
        MockMultipartFile file = new MockMultipartFile("file", "lab_report.pdf", "application/pdf", pdfContent);

        FileValidator.ValidatedFileInfo info = fileValidator.validate(file);
        assertEquals("application/pdf", info.detectedMimeType());
        assertEquals("lab_report.pdf", info.sanitizedFilename());
    }

    @Test
    @DisplayName("Should accept valid PNG file with magic bytes")
    void testValidPng() {
        byte[] pngContent = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "scan.png", "image/png", pngContent);

        FileValidator.ValidatedFileInfo info = fileValidator.validate(file);
        assertEquals("image/png", info.detectedMimeType());
        assertEquals("scan.png", info.sanitizedFilename());
    }

    @Test
    @DisplayName("Should accept valid JPEG file with magic bytes")
    void testValidJpeg() {
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpegContent);

        FileValidator.ValidatedFileInfo info = fileValidator.validate(file);
        assertEquals("image/jpeg", info.detectedMimeType());
        assertEquals("photo.jpg", info.sanitizedFilename());
    }

    @Test
    @DisplayName("Should reject unsupported or malicious file types (e.g. text/exe)")
    void testRejectUnsupportedFileType() {
        byte[] fakeContent = "This is a plain text file, not a medical document.".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "malicious.txt", "text/plain", fakeContent);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> fileValidator.validate(file));
        assertTrue(ex.getMessage().contains("Unsupported file type"));
    }

    @Test
    @DisplayName("Should reject empty file")
    void testRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> fileValidator.validate(file));
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("Should sanitize dangerous filenames preventing directory traversal")
    void testSanitizeFilename() {
        String dangerous = "../../../etc/passwd.pdf";
        String clean = fileValidator.sanitizeFilename(dangerous);
        assertFalse(clean.contains(".."));
        assertFalse(clean.contains("/"));
        assertTrue(clean.contains("passwd.pdf"));
    }
}
