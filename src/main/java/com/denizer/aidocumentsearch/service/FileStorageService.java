package com.denizer.aidocumentsearch.service;

import com.denizer.aidocumentsearch.exception.DocumentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Locale;
import java.util.List;

@Service
public class FileStorageService {

    private final Path storageLocation =
            Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() throws IOException {
        Files.createDirectories(storageLocation);
    }

    public void storeFile(String documentId, MultipartFile file)
            throws IOException {

        String extension = getFileExtension(file.getOriginalFilename());

        Path targetLocation = storageLocation
                .resolve(documentId + extension)
                .normalize();

        if (!targetLocation.startsWith(storageLocation)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        Files.copy(
                file.getInputStream(),
                targetLocation,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    public String getStoragePath(String documentId, String fileName) {
        String extension = getFileExtension(fileName);

        return storageLocation
                .resolve(documentId + extension)
                .normalize()
                .toString();
    }

    public String readFile(String documentId) throws IOException {

        for (String extension : List.of(".txt", ".log")) {

            Path filePath = storageLocation
                    .resolve(documentId + extension)
                    .normalize();

            if (!filePath.startsWith(storageLocation)) {
                throw new IllegalArgumentException("Invalid file path");
            }

            if (Files.exists(filePath)) {
                return Files.readString(filePath);
            }
        }

        throw new IllegalArgumentException(
                "Document not found: " + documentId
        );
    }

    private String getFileExtension(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        return fileName.substring(fileName.lastIndexOf('.'))
                .toLowerCase(Locale.ROOT);
    }

    public String readFileFromPath(String storagePath) throws IOException {

        Path filePath = Paths.get(storagePath).toAbsolutePath().normalize();

        if (!filePath.startsWith(storageLocation)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        if (!Files.exists(filePath)) {
            throw new DocumentNotFoundException("Document file not found");
        }

        return Files.readString(filePath);
    }
}