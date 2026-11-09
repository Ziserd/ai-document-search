package com.denizer.aidocumentsearch.dto;

import java.util.List;

public record DocumentSearchResponse(
        String documentId,
        String keyword,
        int totalMatches,
        int totalOccurrences,
        List<DocumentSearchResult> results
) {
}