package com.medilens.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

public interface StorageService {
    
    String storeReport(UUID userId, UUID reportId, MultipartFile file, String sanitizedFilename);
    
    String storePageImage(UUID userId, UUID reportId, int pageNumber, byte[] imageBytes);
    
    Path loadPath(String relativeOrAbsolutePath);
    
    Resource loadAsResource(String relativeOrAbsolutePath);
    
    String computeSha256(MultipartFile file);
    
    void delete(String relativeOrAbsolutePath);
}
