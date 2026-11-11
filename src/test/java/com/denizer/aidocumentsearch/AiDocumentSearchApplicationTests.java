
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

@SpringBootTest
@Transactional
class AiDocumentSearchApplicationTests {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentSearchService documentSearchService;

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


}
