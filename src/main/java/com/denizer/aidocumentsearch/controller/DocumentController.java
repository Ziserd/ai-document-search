package com.denizer.aidocumentsearch.controller;

import com.denizer.aidocumentsearch.dto.DocumentUploadResponse;
import com.denizer.aidocumentsearch.service.DocumentSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.denizer.aidocumentsearch.dto.DocumentSearchResponse;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentSearchService documentSearchService;

    public DocumentController(
            DocumentSearchService documentSearchService) {
        this.documentSearchService = documentSearchService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file)
            throws IOException {

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
}