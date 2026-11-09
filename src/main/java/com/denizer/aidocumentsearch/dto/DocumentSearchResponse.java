package com.denizer.aidocumentsearch.dto;

import java.util.List;

public record DocumentSearchResponse(
        String documentId,
        String keyword,
        int totalMatchesLine,
        int totalOccurrences,
        List<DocumentSearchResult> results
) {
}