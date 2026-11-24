
package com.denizer.aidocumentsearch.controller;

import com.denizer.aidocumentsearch.dto.DocumentUploadResponse;
import com.denizer.aidocumentsearch.dto.DocumentSearchResponse;
import com.denizer.aidocumentsearch.dto.SemanticSearchResponse;
import com.denizer.aidocumentsearch.service.DocumentSearchService;
import com.denizer.aidocumentsearch.service.RagService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.denizer.aidocumentsearch.dto.AskQuestionRequest;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import com.denizer.aidocumentsearch.dto.RagResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentSearchService documentSearchService;
    private final RagService ragService;

    public DocumentController(
            DocumentSearchService documentSearchService,
            RagService ragService
    ) {
        this.documentSearchService = documentSearchService;
        this.ragService = ragService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        DocumentUploadResponse response =
                documentSearchService.uploadDocument(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{documentId}/search")
    public ResponseEntity<DocumentSearchResponse> searchDocument(
            @PathVariable String documentId,
            @RequestParam String keyword
    ) {

        DocumentSearchResponse response =
                documentSearchService.searchDocument(
                        documentId,
                        keyword
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}/semantic-search")
    public ResponseEntity<SemanticSearchResponse> semanticSearch(
            @PathVariable String documentId,
            @RequestParam String query
    ) {

        SemanticSearchResponse response =
                documentSearchService.semanticSearch(
                        documentId,
                        query
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{documentId}/ask")
    public ResponseEntity<RagResponse> askQuestion(
            @PathVariable String documentId,
            @Valid @RequestBody AskQuestionRequest request
    ) {
        RagResponse response = ragService.askQuestion(
                documentId,
                request.question()
        );

        return ResponseEntity.ok(response);
    }
}
