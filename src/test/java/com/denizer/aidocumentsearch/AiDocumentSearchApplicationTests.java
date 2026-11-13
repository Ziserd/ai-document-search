
package com.denizer.aidocumentsearch;

import com.denizer.aidocumentsearch.dto.DocumentUploadResponse;
import com.denizer.aidocumentsearch.entity.Document;
import com.denizer.aidocumentsearch.exception.DocumentNotFoundException;
import com.denizer.aidocumentsearch.repository.DocumentRepository;
import com.denizer.aidocumentsearch.service.DocumentSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.denizer.aidocumentsearch.exception.DocumentNotFoundException;

import com.denizer.aidocumentsearch.entity.DocumentChunk;
import com.denizer.aidocumentsearch.repository.DocumentChunkRepository;
import java.util.stream.IntStream;
import java.util.List;

@SpringBootTest
@Transactional
class AiDocumentSearchApplicationTests {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentSearchService documentSearchService;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldUploadDocumentAndSaveMetadataToDatabase() throws IOException {
        // Given
        String content = """
                Application started successfully.
                ERROR: Database connection failed.
                User logged in successfully.
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "integration-test.txt",
                "text/plain",
                content.getBytes()
        );

        // When
        DocumentUploadResponse response =
                documentSearchService.uploadDocument(file);

        // Then: Upload response
        assertThat(response.documentId()).isNotBlank();
        assertThat(response.fileName()).isEqualTo("integration-test.txt");
        assertThat(response.totalLines()).isEqualTo(3);
        assertThat(response.status()).isEqualTo("UPLOADED");

        // Then: PostgreSQL record
        Document savedDocument = documentRepository
                .findByDocumentId(response.documentId())
                .orElseThrow();

        assertThat(savedDocument.getFileName())
                .isEqualTo("integration-test.txt");
        assertThat(savedDocument.getFileType()).isEqualTo("txt");
        assertThat(savedDocument.getTotalLines()).isEqualTo(3);
        assertThat(savedDocument.getStoragePath()).isNotBlank();

        // Then: Physical file
        assertThat(Files.exists(Path.of(savedDocument.getStoragePath())))
                .isTrue();
    }


    @Test
    void shouldSearchDocumentUsingDatabaseMetadata() throws IOException {
        // Given
        String content = """
            Application started successfully.
            ERROR: Database connection failed.
            User logged in successfully.
            ERROR: Timeout occurred.
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "search-test.txt",
                "text/plain",
                content.getBytes()
        );

        DocumentUploadResponse uploadResponse =
                documentSearchService.uploadDocument(file);

        // When
        var searchResponse = documentSearchService.searchDocument(
                uploadResponse.documentId(),
                "ERROR"
        );

        // Then
        assertThat(searchResponse.documentId())
                .isEqualTo(uploadResponse.documentId());

        assertThat(searchResponse.keyword()).isEqualTo("ERROR");
        assertThat(searchResponse.totalMatchesLine()).isEqualTo(2);
        assertThat(searchResponse.totalOccurrences()).isEqualTo(2);

        assertThat(searchResponse.results()).hasSize(2);

        assertThat(searchResponse.results().get(0).lineNumber())
                .isEqualTo(2);

        assertThat(searchResponse.results().get(1).lineNumber())
                .isEqualTo(4);
    }


    @Test
    void shouldThrowExceptionWhenDocumentDoesNotExist() {
        // Given
        String nonExistingDocumentId =
                "00000000-0000-0000-0000-000000000000";

        // When & Then
        assertThatThrownBy(() ->
                documentSearchService.searchDocument(
                        nonExistingDocumentId,
                        "ERROR"
                )
        ).isInstanceOf(DocumentNotFoundException.class);
    }


    @Test
    void shouldThrowExceptionWhenPhysicalFileDoesNotExist() throws IOException {
        // Given
        String content = "ERROR: Test file content";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "missing-file-test.txt",
                "text/plain",
                content.getBytes()
        );

        DocumentUploadResponse response =
                documentSearchService.uploadDocument(file);

        Document savedDocument = documentRepository
                .findByDocumentId(response.documentId())
                .orElseThrow();

        // Delete physical file
        Files.delete(Path.of(savedDocument.getStoragePath()));

        // When & Then
        assertThatThrownBy(() ->
                documentSearchService.searchDocument(
                        response.documentId(),
                        "ERROR"
                )
        ).isInstanceOf(DocumentNotFoundException.class);
    }


    @Test
    void shouldSearchCaseInsensitivelyAndCountOccurrences() throws IOException {
        // Given
        String content = """
            ERROR: First error occurred.
            error: Second error occurred.
            This line has no matching keyword.
            ERROR ERROR: Multiple errors occurred.
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "case-insensitive-test.txt",
                "text/plain",
                content.getBytes()
        );

        DocumentUploadResponse response =
                documentSearchService.uploadDocument(file);

        // When
        var searchResponse = documentSearchService.searchDocument(
                response.documentId(),
                "error"
        );

        // Then
        assertThat(searchResponse.totalMatchesLine()).isEqualTo(3);
        assertThat(searchResponse.totalOccurrences()).isEqualTo(7);
        assertThat(searchResponse.results()).hasSize(3);
    }


    @Test
    void shouldCreateAndSaveChunksWhenDocumentIsUploaded() throws IOException {
        // Given
        String content = IntStream.rangeClosed(1, 40)
                .mapToObj(i -> "Line " + i)
                .collect(java.util.stream.Collectors.joining("\n"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "chunk-test.txt",
                "text/plain",
                content.getBytes()
        );

        // When
        DocumentUploadResponse response =
                documentSearchService.uploadDocument(file);

        // Then
        List<DocumentChunk> chunks =
                documentChunkRepository.findByDocumentDocumentId(
                        response.documentId()
                );

        assertThat(chunks).hasSize(3);

        assertThat(chunks.get(0).getStartLine()).isEqualTo(1);
        assertThat(chunks.get(0).getEndLine()).isEqualTo(20);

        assertThat(chunks.get(1).getStartLine()).isEqualTo(18);
        assertThat(chunks.get(1).getEndLine()).isEqualTo(37);

        assertThat(chunks.get(2).getStartLine()).isEqualTo(35);
        assertThat(chunks.get(2).getEndLine()).isEqualTo(40);

        assertThat(chunks.get(0).getContent())
                .contains("Line 1", "Line 20");

        assertThat(chunks.get(1).getContent())
                .contains("Line 18", "Line 37");

        assertThat(chunks.get(2).getContent())
                .contains("Line 35", "Line 40");
    }



    @Test
    void shouldCreateSingleChunkForSmallDocument() throws IOException {
        // Given
        String content = """
            Line 1
            Line 2
            Line 3
            Line 4
            Line 5
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "small-document.txt",
                "text/plain",
                content.getBytes()
        );

        // When
        DocumentUploadResponse response =
                documentSearchService.uploadDocument(file);

        // Then
        List<DocumentChunk> chunks =
                documentChunkRepository.findByDocumentDocumentId(
                        response.documentId()
                );

        assertThat(chunks).hasSize(1);

        DocumentChunk chunk = chunks.get(0);

        assertThat(chunk.getStartLine()).isEqualTo(1);
        assertThat(chunk.getEndLine()).isEqualTo(5);
        assertThat(chunk.getContent()).contains("Line 1", "Line 5");
    }


    @Test
    void shouldCreateOneChunkForExactlyTwentyLines() throws IOException {
        // Given
        String content = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> "Line " + i)
                .collect(java.util.stream.Collectors.joining("\n"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "exactly-twenty-lines.txt",
                "text/plain",
                content.getBytes()
        );

        // When
        DocumentUploadResponse response =
                documentSearchService.uploadDocument(file);

        // Then
        List<DocumentChunk> chunks =
                documentChunkRepository.findByDocumentDocumentId(
                        response.documentId()
                );

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getStartLine()).isEqualTo(1);
        assertThat(chunks.get(0).getEndLine()).isEqualTo(20);
    }



}
