package com.university.backend.hr.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class CvFileStorageService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "png", "jpg", "jpeg", "webp"
    );

    @Value("${hr.cv.storage.upload-dir:uploads/hr-cv}")
    private String uploadDir;

    @Value("${hr.cv.storage.max-bytes:10485760}")
    private long maxBytes;

    public StoredCvFile store(String candidateId, MultipartFile multipartFile) {
        validateFile(multipartFile);
        try {
            Path uploadRoot = Path.of(uploadDir == null || uploadDir.isBlank()
                    ? "uploads/hr-cv"
                    : uploadDir);
            Files.createDirectories(uploadRoot);

            String originalName = multipartFile.getOriginalFilename();
            String extension = extensionOf(originalName);
            String safeName = "candidate-" + candidateId + "-" + UUID.randomUUID() + "." + extension;
            Path target = uploadRoot.resolve(safeName);

            try (InputStream in = multipartFile.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return new StoredCvFile(
                    originalName == null || originalName.isBlank() ? safeName : originalName,
                    multipartFile.getContentType(),
                    multipartFile.getSize(),
                    target.toAbsolutePath().toString()
            );
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store CV file", ex);
        }
    }

    public byte[] read(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No CV file uploaded");
        }
        try {
            Path path = Path.of(absolutePath);
            if (!Files.exists(path)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV file does not exist on disk");
            }
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read CV file", ex);
        }
    }

    public void deleteIfExists(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(absolutePath));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete CV file", ex);
        }
    }

    private void validateFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file is required");
        }

        long effectiveMaxBytes = maxBytes <= 0 ? 10_485_760L : maxBytes;
        if (multipartFile.getSize() > effectiveMaxBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large");
        }

        String contentType = multipartFile.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
        }

        String extension = extensionOf(multipartFile.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file extension");
        }
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    public record StoredCvFile(
            String originalFileName,
            String contentType,
            long sizeBytes,
            String absolutePath
    ) {
    }
}
