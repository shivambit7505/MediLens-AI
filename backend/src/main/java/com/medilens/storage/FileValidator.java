package com.medilens.storage;

import com.medilens.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Component
public class FileValidator {

    public static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB

    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D}; // %PDF-
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    public record ValidatedFileInfo(String detectedMimeType, String sanitizedFilename) {}

    public ValidatedFileInfo validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File size exceeds 20MB limit: " + (file.getSize() / (1024 * 1024)) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "uploaded_report.pdf";
        }

        String sanitizedFilename = sanitizeFilename(originalFilename);
        String detectedMimeType = detectMimeTypeFromMagicBytes(file);

        return new ValidatedFileInfo(detectedMimeType, sanitizedFilename);
    }

    public String sanitizeFilename(String filename) {
        String clean = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        while (clean.contains("..")) {
            clean = clean.replace("..", "_");
        }
        return clean;
    }

    public String detectMimeTypeFromMagicBytes(MultipartFile file) {
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 3) {
                throw new BadRequestException("File header is too small to identify format");
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to read file stream: " + e.getMessage());
        }

        if (startsWith(header, PDF_MAGIC)) {
            return "application/pdf";
        }
        if (startsWith(header, PNG_MAGIC)) {
            return "image/png";
        }
        if (startsWith(header, JPEG_MAGIC)) {
            return "image/jpeg";
        }

        throw new BadRequestException("Unsupported file type. Only PDF, PNG, and JPEG documents are permitted.");
    }

    private boolean startsWith(byte[] source, byte[] prefix) {
        if (source.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (source[i] != prefix[i]) return false;
        }
        return true;
    }
}
