package com.medilens.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalStorageServiceImpl(tempDir.toString());
    }

    @Test
    @DisplayName("Should compute accurate deterministic SHA-256 hash")
    void testComputeSha256() {
        byte[] content = "Sample medical report content for hashing".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", content);

        String hash1 = storageService.computeSha256(file);
        String hash2 = storageService.computeSha256(file);

        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should store report file safely within user-scoped directory")
    void testStoreReport() throws IOException {
        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        byte[] content = "Report document bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "cbc.pdf", "application/pdf", content);

        String storedPath = storageService.storeReport(userId, reportId, file, "cbc.pdf");
        assertNotNull(storedPath);
        assertTrue(Files.exists(Path.of(storedPath)));
        assertArrayEquals(content, Files.readAllBytes(Path.of(storedPath)));
    }

    @Test
    @DisplayName("Should store page image in pages subdirectory")
    void testStorePageImage() throws IOException {
        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};

        String pagePath = storageService.storePageImage(userId, reportId, 1, imageBytes);
        assertNotNull(pagePath);
        assertTrue(Files.exists(Path.of(pagePath)));
        assertTrue(pagePath.contains("page_001.png"));
    }
}
