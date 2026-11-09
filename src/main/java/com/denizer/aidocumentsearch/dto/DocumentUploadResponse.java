package com.denizer.aidocumentsearch.dto;

public record DocumentUploadResponse(
        String documentId,
        String fileName,
        int totalLines,
        String status
) {
}