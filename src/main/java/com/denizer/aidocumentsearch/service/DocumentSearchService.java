package com.denizer.aidocumentsearch.service;

import com.denizer.aidocumentsearch.dto.DocumentUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.denizer.aidocumentsearch.dto.DocumentSearchResponse;
import com.denizer.aidocumentsearch.dto.DocumentSearchResult;
import java.util.ArrayList;

@Service
public class DocumentSearchService {

    private final FileStorageService fileStorageService;

    public DocumentSearchService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public DocumentUploadResponse uploadDocument(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "File size cannot exceed 5 MB"
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String lowerCaseName = fileName.toLowerCase(Locale.ROOT);

        if (!lowerCaseName.endsWith(".txt")
                && !lowerCaseName.endsWith(".log")) {
            throw new IllegalArgumentException(
                    "Only TXT and LOG files are supported"
            );
        }

        String content = new String(
                file.getBytes(),
                StandardCharsets.UTF_8
        );

        List<String> lines = content.lines().toList();

        String documentId = UUID.randomUUID().toString();

        fileStorageService.storeFile(documentId, file);

        return new DocumentUploadResponse(
                documentId,
                fileName,
                lines.size(),
                "UPLOADED"
        );
    }

    public DocumentSearchResponse searchDocument(
            String documentId,
            String keyword
    ) {

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException(
                    "Keyword cannot be empty"
            );
        }

        String content;

        try {
            content = fileStorageService.readFile(documentId);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Document could not be read: " + documentId, e
            );
        }

        List<String> lines = content.lines().toList();

        List<DocumentSearchResult> results = new ArrayList<>();
        int totalOccurrences = 0 ;

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

            String lowerLine = line.toLowerCase(Locale.ROOT);
            String lowerKeyword = keyword.toLowerCase(Locale.ROOT);

            int occurrences = 0;
            int index = 0;

            while ((index = lowerLine.indexOf(lowerKeyword, index)) != -1) {
                occurrences++;
                index += lowerKeyword.length();
            }

            if (occurrences > 0) {

                totalOccurrences += occurrences;

                results.add(
                        new DocumentSearchResult(
                                i + 1,
                                line
                        )
                );
            }
        }

        return new DocumentSearchResponse(
                documentId,
                keyword,
                results.size(),
                totalOccurrences,
                results
        );
    }
}