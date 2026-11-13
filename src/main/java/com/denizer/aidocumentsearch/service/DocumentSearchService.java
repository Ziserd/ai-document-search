package com.denizer.aidocumentsearch.service;

import com.denizer.aidocumentsearch.dto.DocumentUploadResponse;
import com.denizer.aidocumentsearch.entity.Document;
import com.denizer.aidocumentsearch.exception.DocumentNotFoundException;
import com.denizer.aidocumentsearch.repository.DocumentRepository;
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

import com.denizer.aidocumentsearch.entity.DocumentChunk;
import com.denizer.aidocumentsearch.repository.DocumentChunkRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentSearchService {

    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentChunkingService documentChunkingService;

    public DocumentSearchService(
            FileStorageService fileStorageService,
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            DocumentChunkingService documentChunkingService
    ) {
        this.fileStorageService = fileStorageService;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.documentChunkingService = documentChunkingService;
    }

    @Transactional
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

        Document document = new Document();
        document.setDocumentId(documentId);
        document.setFileName(fileName);
        document.setFileType(
                lowerCaseName.substring(lowerCaseName.lastIndexOf('.') + 1)
        );
        document.setTotalLines(lines.size());
        document.setStoragePath(
                fileStorageService.getStoragePath(documentId, fileName)
        );

        Document savedDocument = documentRepository.save(document);

        List<DocumentChunk> chunks =
                documentChunkingService.createChunks(savedDocument, lines);

        documentChunkRepository.saveAll(chunks);

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

        Document document = documentRepository
                .findByDocumentId(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        "Document not found: " + documentId
                ));

        String content;

        try {
            content = fileStorageService.readFileFromPath(
                    document.getStoragePath()
            );
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Document could not be read: " + documentId, e
            );
        }

        List<String> lines = content.lines().toList();

        List<DocumentSearchResult> results = new ArrayList<>();
        int totalOccurrences = 0;

        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);
            String lowerLine = line.toLowerCase(Locale.ROOT);

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