package com.medilens.storage;

import com.medilens.exception.BadRequestException;
import com.medilens.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class LocalStorageServiceImpl implements StorageService {

    private final Path rootLocation;

    public LocalStorageServiceImpl(@Value("${medilens.storage.reports-dir:./data/reports}") String reportsDir) {
        this.rootLocation = Paths.get(reportsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory: " + this.rootLocation, e);
        }
    }

    @Override
    public String storeReport(UUID userId, UUID reportId, MultipartFile file, String sanitizedFilename) {
        try {
            Path userDir = rootLocation.resolve(userId.toString()).resolve(reportId.toString()).normalize();
            Files.createDirectories(userDir);

            Path destinationFile = userDir.resolve(sanitizedFilename).normalize();
            if (!destinationFile.startsWith(rootLocation)) {
                throw new BadRequestException("Cannot store file outside target directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return destinationFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store report file", e);
        }
    }

    @Override
    public String storePageImage(UUID userId, UUID reportId, int pageNumber, byte[] imageBytes) {
        try {
            Path pagesDir = rootLocation.resolve(userId.toString())
                    .resolve(reportId.toString())
                    .resolve("pages")
                    .normalize();
            Files.createDirectories(pagesDir);

            Path pageFile = pagesDir.resolve(String.format("page_%03d.png", pageNumber)).normalize();
            if (!pageFile.startsWith(rootLocation)) {
                throw new BadRequestException("Cannot store page image outside target directory");
            }

            Files.write(pageFile, imageBytes);
            return pageFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store page image for page: " + pageNumber, e);
        }
    }

    @Override
    public Path loadPath(String pathString) {
        Path path = Paths.get(pathString).toAbsolutePath().normalize();
        if (!path.startsWith(rootLocation) && !Files.exists(path)) {
            // Also try resolving relative to rootLocation
            path = rootLocation.resolve(pathString).normalize();
        }
        return path;
    }

    @Override
    public Resource loadAsResource(String pathString) {
        try {
            Path file = loadPath(pathString);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File", "path", pathString);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File", "path", pathString);
        }
    }

    @Override
    public String computeSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            byte[] hashBytes = digest.digest();
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Failed to calculate SHA-256 hash for file", e);
        }
    }

    @Override
    public void delete(String pathString) {
        try {
            Path file = loadPath(pathString);
            Files.deleteIfExists(file);
        } catch (IOException ignored) {}
    }
}
